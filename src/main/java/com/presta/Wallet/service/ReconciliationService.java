package com.presta.Wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.presta.Wallet.dto.ReconciliationItemResponse;
import com.presta.Wallet.dto.ReconciliationReportResponse;
import com.presta.Wallet.dto.ReconciliationSummary;
import com.presta.Wallet.entity.ExternalTransaction;
import com.presta.Wallet.entity.ReconciliationItem;
import com.presta.Wallet.entity.ReconciliationReport;
import com.presta.Wallet.entity.WalletTransaction;
import com.presta.Wallet.repository.ExternalTransactionRepository;
import com.presta.Wallet.repository.ReconciliationItemRepository;
import com.presta.Wallet.repository.ReconciliationReportRepository;
import com.presta.Wallet.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final ExternalTransactionRepository externalTransactionRepository;
    private final ReconciliationReportRepository reconciliationReportRepository;
    private final ReconciliationItemRepository reconciliationItemRepository;

    @Transactional
    public ReconciliationReportResponse performReconciliation(LocalDate date) {
        log.info("Starting reconciliation process for date: {}", date);

        // Check if reconciliation already exists for this date
        Optional<ReconciliationReport> existingReport = reconciliationReportRepository.findByReconciliationDate(date);
        if (existingReport.isPresent()) {
            log.info("Reconciliation already exists for date: {}, returning existing report", date);
            return mapToResponse(existingReport.get());
        }

        List<WalletTransaction> internalTransactions = getInternalTransactionsForDate(date);
        List<ExternalTransaction> externalTransactions = externalTransactionRepository.findByTransactionDate(date);

        log.info("Found {} internal transactions and {} external transactions for date: {}", 
                internalTransactions.size(), externalTransactions.size(), date);

        // reconciliation report
        ReconciliationReport report = ReconciliationReport.builder()
                .reconciliationDate(date)
                .totalInternalTransactions(internalTransactions.size())
                .totalExternalTransactions(externalTransactions.size())
                .matchedTransactions(0)
                .unmatchedInternal(0)
                .unmatchedExternal(0)
                .amountDifferences(0)
                .totalInternalAmount(calculateTotalAmount(internalTransactions))
                .totalExternalAmount(calculateExternalTotalAmount(externalTransactions))
                .status(ReconciliationReport.ReconciliationStatus.IN_PROGRESS)
                .build();

        report.setDifferenceAmount(
            report.getTotalInternalAmount().subtract(report.getTotalExternalAmount())
        );

        ReconciliationReport savedReport = reconciliationReportRepository.save(report);

        
        List<ReconciliationItem> reconciliationItems = performMatching(
            internalTransactions, externalTransactions, savedReport
        );

    
        updateReportStatistics(savedReport, reconciliationItems);
        savedReport.markCompleted();
        reconciliationReportRepository.save(savedReport);

        log.info("Reconciliation completed for date: {} with {} items", date, reconciliationItems.size());
        return mapToResponse(savedReport);
    }

    private List<WalletTransaction> getInternalTransactionsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        return walletTransactionRepository.findByWalletIdAndDateRange(null, startOfDay, endOfDay)
                .stream()
                .filter(t -> t.getCreatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private List<ReconciliationItem> performMatching(
            List<WalletTransaction> internalTransactions,
            List<ExternalTransaction> externalTransactions,
            ReconciliationReport report) {

        List<ReconciliationItem> items = new ArrayList<>();
        Set<Long> matchedInternalIds = new HashSet<>();
        Set<Long> matchedExternalIds = new HashSet<>();

        //here  Created  maps for efficient lookup
        Map<String, List<WalletTransaction>> internalByReference = internalTransactions.stream()
                .filter(t -> t.getReferenceId() != null)
                .collect(Collectors.groupingBy(WalletTransaction::getReferenceId));

        Map<String, List<ExternalTransaction>> externalByReference = externalTransactions.stream()
                .filter(t -> t.getReferenceId() != null)
                .collect(Collectors.groupingBy(ExternalTransaction::getReferenceId));

        // so  matches (reference ID and amount)
        for (String referenceId : internalByReference.keySet()) {
            if (externalByReference.containsKey(referenceId)) {
                List<WalletTransaction> internalTxns = internalByReference.get(referenceId);
                List<ExternalTransaction> externalTxns = externalByReference.get(referenceId);

                for (WalletTransaction internal : internalTxns) {
                    for (ExternalTransaction external : externalTxns) {
                        if (internal.getAmount().compareTo(external.getAmount()) == 0 &&
                            !matchedInternalIds.contains(internal.getId()) &&
                            !matchedExternalIds.contains(external.getId())) {

                            ReconciliationItem item = createReconciliationItem(
                                report, internal, external, 
                                ReconciliationItem.MatchType.PERFECT_MATCH,
                                ReconciliationItem.DiscrepancyType.NONE,
                                "Perfect match on reference and amount"
                            );
                            items.add(item);

                            matchedInternalIds.add(internal.getId());
                            matchedExternalIds.add(external.getId());
                            break;
                        }
                    }
                }
            }
        }

        // matches with amount differences
        for (String referenceId : internalByReference.keySet()) {
            if (externalByReference.containsKey(referenceId)) {
                List<WalletTransaction> internalTxns = internalByReference.get(referenceId);
                List<ExternalTransaction> externalTxns = externalByReference.get(referenceId);

                for (WalletTransaction internal : internalTxns) {
                    if (matchedInternalIds.contains(internal.getId())) continue;

                    for (ExternalTransaction external : externalTxns) {
                        if (matchedExternalIds.contains(external.getId())) continue;

                        ReconciliationItem item = createReconciliationItem(
                            report, internal, external,
                            ReconciliationItem.MatchType.REFERENCE_MATCH,
                            ReconciliationItem.DiscrepancyType.AMOUNT_DIFFERENCE,
                            String.format("Reference match but amount differs by %s", 
                                internal.getAmount().subtract(external.getAmount()))
                        );
                        items.add(item);

                        matchedInternalIds.add(internal.getId());
                        matchedExternalIds.add(external.getId());
                        break;
                    }
                }
            }
        }

        // Unmatched internal transactions
        for (WalletTransaction internal : internalTransactions) {
            if (!matchedInternalIds.contains(internal.getId())) {
                ReconciliationItem item = createReconciliationItem(
                    report, internal, null,
                    ReconciliationItem.MatchType.NO_MATCH,
                    ReconciliationItem.DiscrepancyType.MISSING_EXTERNAL,
                    "Internal transaction with no external match"
                );
                items.add(item);
            }
        }

        // here let Unmatched external transactions
        for (ExternalTransaction external : externalTransactions) {
            if (!matchedExternalIds.contains(external.getId())) {
                ReconciliationItem item = createReconciliationItem(
                    report, null, external,
                    ReconciliationItem.MatchType.NO_MATCH,
                    ReconciliationItem.DiscrepancyType.MISSING_INTERNAL,
                    "External transaction with no internal match"
                );
                items.add(item);
            }
        }

        return reconciliationItemRepository.saveAll(items);
    }

    private ReconciliationItem createReconciliationItem(
            ReconciliationReport report,
            WalletTransaction internal,
            ExternalTransaction external,
            ReconciliationItem.MatchType matchType,
            ReconciliationItem.DiscrepancyType discrepancyType,
            String notes) {

        ReconciliationItem.ReconciliationItemBuilder builder = ReconciliationItem.builder()
                .reconciliationReport(report)
                .matchType(matchType)
                .discrepancyType(discrepancyType)
                .notes(notes);

        if (internal != null) {
            builder.internalTransactionId(internal.getId())
                   .internalAmount(internal.getAmount())
                   .referenceId(internal.getReferenceId());
        }

        if (external != null) {
            builder.externalTransactionId(external.getId())
                   .externalAmount(external.getAmount());
            
            if (internal == null) {
                builder.referenceId(external.getReferenceId());
            }
        }

        if (internal != null && external != null) {
            builder.amountDifference(internal.getAmount().subtract(external.getAmount()));
        }

        return builder.build();
    }

    private void updateReportStatistics(ReconciliationReport report, List<ReconciliationItem> items) {
        int perfectMatches = 0;
        int unmatchedInternal = 0;
        int unmatchedExternal = 0;
        int amountDifferences = 0;

        for (ReconciliationItem item : items) {
            switch (item.getMatchType()) {
                case PERFECT_MATCH -> perfectMatches++;
                case REFERENCE_MATCH -> amountDifferences++;
                case NO_MATCH -> {
                    if (item.getInternalTransactionId() != null && item.getExternalTransactionId() == null) {
                        unmatchedInternal++;
                    } else if (item.getInternalTransactionId() == null && item.getExternalTransactionId() != null) {
                        unmatchedExternal++;
                    }
                }
                case AMOUNT_MATCH -> throw new UnsupportedOperationException("Unimplemented case: " + item.getMatchType());
                default -> throw new IllegalArgumentException("Unexpected value: " + item.getMatchType());
            }
        }

        report.setMatchedTransactions(perfectMatches);
        report.setUnmatchedInternal(unmatchedInternal);
        report.setUnmatchedExternal(unmatchedExternal);
        report.setAmountDifferences(amountDifferences);
    }

    private BigDecimal calculateTotalAmount(List<WalletTransaction> transactions) {
        return transactions.stream()
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateExternalTotalAmount(List<ExternalTransaction> transactions) {
        return transactions.stream()
                .map(ExternalTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public ReconciliationReportResponse getReconciliationReport(LocalDate date) {
        log.info("Fetching reconciliation report for date: {}", date);

        ReconciliationReport report = reconciliationReportRepository.findByReconciliationDate(date)
                .orElseThrow(() -> new RuntimeException("No reconciliation report found for date: " + date));

        return mapToResponse(report);
    }

    @Transactional(readOnly = true)
    public List<ReconciliationReportResponse> getReconciliationHistory(int limit) {
        log.info("Fetching reconciliation history with limit: {}", limit);

        LocalDate fromDate = LocalDate.now().minusDays(limit);
        List<ReconciliationReport> reports = reconciliationReportRepository.findRecentReports(fromDate);

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReconciliationReportResponse mapToResponse(ReconciliationReport report) {
        List<ReconciliationItem> discrepancies = reconciliationItemRepository
                .findDiscrepanciesByReportId(report.getId());

        ReconciliationSummary summary = ReconciliationSummary.builder()
                .totalInternalTransactions(report.getTotalInternalTransactions())
                .totalExternalTransactions(report.getTotalExternalTransactions())
                .matchedTransactions(report.getMatchedTransactions())
                .unmatchedInternal(report.getUnmatchedInternal())
                .unmatchedExternal(report.getUnmatchedExternal())
                .amountDifferences(report.getAmountDifferences())
                .totalInternalAmount(report.getTotalInternalAmount())
                .totalExternalAmount(report.getTotalExternalAmount())
                .differenceAmount(report.getDifferenceAmount())
                .build();

        List<ReconciliationItemResponse> discrepancyResponses = discrepancies.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return ReconciliationReportResponse.builder()
                .reportId(report.getId())
                .reconciliationDate(report.getReconciliationDate())
                .summary(summary)
                .discrepancies(discrepancyResponses)
                .fileName(report.getFileName())
                .providerName(report.getProviderName())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .completedAt(report.getCompletedAt())
                .build();
    }

    private ReconciliationItemResponse mapItemToResponse(ReconciliationItem item) {
        return ReconciliationItemResponse.builder()
                .itemId(item.getId())
                .referenceId(item.getReferenceId())
                .matchType(item.getMatchType().name())
                .discrepancyType(item.getDiscrepancyType().name())
                .internalTransactionId(item.getInternalTransactionId())
                .externalTransactionId(item.getExternalTransactionId())
                .internalAmount(item.getInternalAmount())
                .externalAmount(item.getExternalAmount())
                .amountDifference(item.getAmountDifference())
                .notes(item.getNotes())
                .build();
    }
}
