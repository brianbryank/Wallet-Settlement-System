package com.presta.Wallet.creation.microservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.presta.Wallet.entity.ReconciliationReport;
import com.presta.Wallet.repository.ReconciliationReportRepository;

@DataJpaTest
@ActiveProfiles("test")
class ReconciliationReportRepositoryTest {

    @Autowired
    private ReconciliationReportRepository reconciliationReportRepository;

    private LocalDate testDate1;
    private LocalDate testDate2;
    private ReconciliationReport report1;
    private ReconciliationReport report2;

    @BeforeEach
    void setUp() {
        reconciliationReportRepository.deleteAll();

        testDate1 = LocalDate.of(2024, 1, 15);
        testDate2 = LocalDate.of(2024, 1, 16);

        report1 = ReconciliationReport.builder()
                .reconciliationDate(testDate1)
                .totalInternalTransactions(10)
                .totalExternalTransactions(10)
                .matchedTransactions(8)
                .unmatchedInternal(1)
                .unmatchedExternal(1)
                .amountDifferences(0)
                .totalInternalAmount(BigDecimal.valueOf(500.00))
                .totalExternalAmount(BigDecimal.valueOf(480.00))
                .differenceAmount(BigDecimal.valueOf(20.00))
                .status(ReconciliationReport.ReconciliationStatus.COMPLETED)
                .providerName("TEST_PROVIDER")
                .build();

        report2 = ReconciliationReport.builder()
                .reconciliationDate(testDate2)
                .totalInternalTransactions(15)
                .totalExternalTransactions(15)
                .matchedTransactions(15)
                .unmatchedInternal(0)
                .unmatchedExternal(0)
                .amountDifferences(0)
                .totalInternalAmount(BigDecimal.valueOf(750.00))
                .totalExternalAmount(BigDecimal.valueOf(750.00))
                .differenceAmount(BigDecimal.ZERO)
                .status(ReconciliationReport.ReconciliationStatus.COMPLETED)
                .providerName("TEST_PROVIDER")
                .build();

        reconciliationReportRepository.save(report1);
        reconciliationReportRepository.save(report2);
    }

    @Test
    void findByReconciliationDate_ExistingDate_ReturnsReport() {
        // When
        Optional<ReconciliationReport> result = reconciliationReportRepository
                .findByReconciliationDate(testDate1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getReconciliationDate()).isEqualTo(testDate1);
        assertThat(result.get().getMatchedTransactions()).isEqualTo(8);
    }

    @Test
    void findByReconciliationDateBetween_DateRange_ReturnsReports() {
        // Given
        LocalDate startDate = testDate1.minusDays(1);
        LocalDate endDate = testDate2.plusDays(1);

        // When
        List<ReconciliationReport> reports = reconciliationReportRepository
                .findByReconciliationDateBetween(startDate, endDate);

        // Then
        assertThat(reports).hasSize(2);
    }

    @Test
    void findByStatus_CompletedStatus_ReturnsCompletedReports() {
        // When
        List<ReconciliationReport> completedReports = reconciliationReportRepository
                .findByStatus(ReconciliationReport.ReconciliationStatus.COMPLETED);

        // Then
        assertThat(completedReports).hasSize(2);
        assertThat(completedReports).allMatch(r -> 
            r.getStatus() == ReconciliationReport.ReconciliationStatus.COMPLETED);
    }

    @Test
    void findAllOrderByDateDesc_ReturnsReportsInDescendingOrder() {
        // When
        List<ReconciliationReport> reports = reconciliationReportRepository
                .findAllOrderByDateDesc();

        // Then
        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).getReconciliationDate()).isEqualTo(testDate2); // Most recent first
        assertThat(reports.get(1).getReconciliationDate()).isEqualTo(testDate1);
    }

    @Test
    void findRecentReports_FromDate_ReturnsRecentReports() {
        // Given
        LocalDate fromDate = testDate1;

        // When
        List<ReconciliationReport> recentReports = reconciliationReportRepository
                .findRecentReports(fromDate);

        // Then
        assertThat(recentReports).hasSize(2);
    }

    @Test
    void existsByReconciliationDate_ExistingDate_ReturnsTrue() {
        // When
        boolean exists = reconciliationReportRepository
                .existsByReconciliationDate(testDate1);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByReconciliationDate_NonExistingDate_ReturnsFalse() {
        // Given
        LocalDate nonExistingDate = LocalDate.of(2024, 1, 20);

        // When
        boolean exists = reconciliationReportRepository
                .existsByReconciliationDate(nonExistingDate);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void countPerfectReconciliations_ReturnsPerfectMatchCount() {
        // When
        long perfectCount = reconciliationReportRepository.countPerfectReconciliations();

        // Then
        assertThat(perfectCount).isEqualTo(1); // Only report2 has perfect reconciliation
    }
}