package com.presta.Wallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.presta.Wallet.entity.ReconciliationItem;
import com.presta.Wallet.entity.ReconciliationReport;
import com.presta.Wallet.repository.ReconciliationItemRepository;
import com.presta.Wallet.repository.ReconciliationReportRepository;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final ReconciliationReportRepository reconciliationReportRepository;
    private final ReconciliationItemRepository reconciliationItemRepository;

    public String exportReconciliationReport(LocalDate date) {
        log.info("Exporting reconciliation report to CSV for date: {}", date);

        ReconciliationReport report = reconciliationReportRepository.findByReconciliationDate(date)
                .orElseThrow(() -> new RuntimeException("No reconciliation report found for date: " + date));

        List<ReconciliationItem> items = reconciliationItemRepository.findByReconciliationReportId(report.getId());

        StringWriter csvWriter = new StringWriter();


        csvWriter.append("Reconciliation Report for ").append(date.toString()).append("\n");
        csvWriter.append("Generated on: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        csvWriter.append("Provider: ").append(report.getProviderName() != null ? report.getProviderName() : "N/A").append("\n");
        csvWriter.append("\n");

        // this where summary is "writesummery"
        csvWriter.append("SUMMARY\n");
        csvWriter.append("Total Internal Transactions,").append(String.valueOf(report.getTotalInternalTransactions())).append("\n");
        csvWriter.append("Total External Transactions,").append(String.valueOf(report.getTotalExternalTransactions())).append("\n");
        csvWriter.append("Matched Transactions,").append(String.valueOf(report.getMatchedTransactions())).append("\n");
        csvWriter.append("Unmatched Internal,").append(String.valueOf(report.getUnmatchedInternal())).append("\n");
        csvWriter.append("Unmatched External,").append(String.valueOf(report.getUnmatchedExternal())).append("\n");
        csvWriter.append("Amount Differences,").append(String.valueOf(report.getAmountDifferences())).append("\n");
        csvWriter.append("Total Internal Amount,").append(report.getTotalInternalAmount().toString()).append("\n");
        csvWriter.append("Total External Amount,").append(report.getTotalExternalAmount().toString()).append("\n");
        csvWriter.append("Difference Amount,").append(report.getDifferenceAmount().toString()).append("\n");
        
        double matchPercentage = report.getTotalInternalTransactions() > 0 ? 
            (report.getMatchedTransactions().doubleValue() / Math.max(report.getTotalInternalTransactions(), report.getTotalExternalTransactions())) * 100.0 : 100.0;
        csvWriter.append("Match Percentage,").append(String.format("%.2f%%", matchPercentage)).append("\n");
        csvWriter.append("\n");

        // detailed items
        csvWriter.append("DETAILED RECONCILIATION ITEMS\n");
        csvWriter.append("Item ID,Reference ID,Match Type,Discrepancy Type,Internal Transaction ID,External Transaction ID,")
                 .append("Internal Amount,External Amount,Amount Difference,Severity,Notes\n");

        for (ReconciliationItem item : items) {
            csvWriter.append(String.valueOf(item.getId())).append(",");
            csvWriter.append(escapeCsv(item.getReferenceId())).append(",");
            csvWriter.append(item.getMatchType().name()).append(",");
            csvWriter.append(item.getDiscrepancyType().name()).append(",");
            csvWriter.append(item.getInternalTransactionId() != null ? item.getInternalTransactionId().toString() : "").append(",");
            csvWriter.append(item.getExternalTransactionId() != null ? item.getExternalTransactionId().toString() : "").append(",");
            csvWriter.append(item.getInternalAmount() != null ? item.getInternalAmount().toString() : "").append(",");
            csvWriter.append(item.getExternalAmount() != null ? item.getExternalAmount().toString() : "").append(",");
            csvWriter.append(item.getAmountDifference() != null ? item.getAmountDifference().toString() : "").append(",");
            csvWriter.append(getSeverity(item)).append(",");
            csvWriter.append(escapeCsv(item.getNotes())).append("\n");
        }

        String csvContent = csvWriter.toString();
        log.info("CSV export completed for date: {}, {} items exported", date, items.size());
        
        return csvContent;
    }

    public String exportTransactionSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Exporting transaction summary from {} to {}", startDate, endDate);

        List<ReconciliationReport> reports = reconciliationReportRepository.findByReconciliationDateBetween(startDate, endDate);

        StringWriter csvWriter = new StringWriter();

        // header
        csvWriter.append("Transaction Summary Report\n");
        csvWriter.append("Period: ").append(startDate.toString()).append(" to ").append(endDate.toString()).append("\n");
        csvWriter.append("Generated on: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        csvWriter.append("\n");

        // column headers
        csvWriter.append("Date,Total Internal,Total External,Matched,Unmatched Internal,Unmatched External,")
                 .append("Amount Differences,Internal Amount,External Amount,Difference,Match %,Status\n");

        for (ReconciliationReport report : reports) {
            double matchPercentage = report.getTotalInternalTransactions() > 0 ? 
                (report.getMatchedTransactions().doubleValue() / Math.max(report.getTotalInternalTransactions(), report.getTotalExternalTransactions())) * 100.0 : 100.0;

            csvWriter.append(report.getReconciliationDate().toString()).append(",");
            csvWriter.append(String.valueOf(report.getTotalInternalTransactions())).append(",");
            csvWriter.append(String.valueOf(report.getTotalExternalTransactions())).append(",");
            csvWriter.append(String.valueOf(report.getMatchedTransactions())).append(",");
            csvWriter.append(String.valueOf(report.getUnmatchedInternal())).append(",");
            csvWriter.append(String.valueOf(report.getUnmatchedExternal())).append(",");
            csvWriter.append(String.valueOf(report.getAmountDifferences())).append(",");
            csvWriter.append(report.getTotalInternalAmount().toString()).append(",");
            csvWriter.append(report.getTotalExternalAmount().toString()).append(",");
            csvWriter.append(report.getDifferenceAmount().toString()).append(",");
            csvWriter.append(String.format("%.2f%%", matchPercentage)).append(",");
            csvWriter.append(report.getStatus().name()).append("\n");
        }

        String csvContent = csvWriter.toString();
        log.info("Transaction summary export completed, {} reports exported", reports.size());
        
        return csvContent;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Escaper quotes and wrap in quotes 
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    private String getSeverity(ReconciliationItem item) {
        if (item.getDiscrepancyType() == null) {
            return "LOW";
        }
        
        return switch (item.getDiscrepancyType()) {
            case MISSING_INTERNAL, MISSING_EXTERNAL -> "HIGH";
            case AMOUNT_DIFFERENCE -> {
                if (item.getAmountDifference() != null && 
                    item.getAmountDifference().abs().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                    yield "HIGH";
                } else if (item.getAmountDifference() != null && 
                          item.getAmountDifference().abs().compareTo(java.math.BigDecimal.valueOf(10)) > 0) {
                    yield "MEDIUM";
                } else {
                    yield "LOW";
                }
            }
            case DUPLICATE_REFERENCE -> "MEDIUM";
            default -> "LOW";
        };
    }

    public String generateSampleExternalReport(LocalDate date, int transactionCount) {
        log.info("Generating sample external report for date: {} with {} transactions", date, transactionCount);

        StringWriter csvWriter = new StringWriter();

        //  CSV header
        csvWriter.append("transaction_id,transaction_date,amount,reference_id,transaction_type,customer_id,service_type,description\n");

    
        for (int i = 1; i <= transactionCount; i++) {
            csvWriter.append("EXT_").append(date.toString().replace("-", "")).append("_").append(String.format("%03d", i)).append(",");
            csvWriter.append(date.toString()).append(",");
            csvWriter.append(String.valueOf(25.00 + (i * 5))).append(",");
            csvWriter.append("REF_").append(String.valueOf(System.currentTimeMillis() + i)).append(",");
            csvWriter.append(i % 3 == 0 ? "TOPUP" : "CONSUMPTION").append(",");
            csvWriter.append("CUST_").append(String.valueOf(i % 5 + 1)).append(",");
            csvWriter.append(i % 2 == 0 ? "KYC" : "CRB").append(",");
            csvWriter.append("Sample external transaction ").append(String.valueOf(i)).append("\n");
        }

        String csvContent = csvWriter.toString();
        log.info("Sample external report generated with {} transactions", transactionCount);
        
        return csvContent;
    }
}