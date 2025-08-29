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

import com.presta.Wallet.entity.ExternalTransaction;
import com.presta.Wallet.repository.ExternalTransactionRepository;

@DataJpaTest
@ActiveProfiles("test")
class ExternalTransactionRepositoryTest {

    @Autowired
    private ExternalTransactionRepository externalTransactionRepository;

    private LocalDate testDate;
    private ExternalTransaction transaction1;
    private ExternalTransaction transaction2;

    @BeforeEach
    void setUp() {
        externalTransactionRepository.deleteAll();
        
        testDate = LocalDate.of(2024, 1, 15);

        transaction1 = ExternalTransaction.builder()
                .externalTransactionId("EXT001")
                .transactionDate(testDate)
                .amount(BigDecimal.valueOf(50.00))
                .referenceId("REF001")
                .transactionType("TOPUP")
                .customerId("CUST001")
                .serviceType("BANK_TRANSFER")
                .providerName("TEST_PROVIDER")
                .fileName("test_file.csv")
                .status(ExternalTransaction.ProcessingStatus.PENDING)
                .build();

        transaction2 = ExternalTransaction.builder()
                .externalTransactionId("EXT002")
                .transactionDate(testDate)
                .amount(BigDecimal.valueOf(25.00))
                .referenceId("REF002")
                .transactionType("CONSUMPTION")
                .customerId("CUST002")
                .serviceType("CRB")
                .providerName("TEST_PROVIDER")
                .fileName("test_file.csv")
                .status(ExternalTransaction.ProcessingStatus.PROCESSED)
                .build();

        externalTransactionRepository.save(transaction1);
        externalTransactionRepository.save(transaction2);
    }

    @Test
    void findByTransactionDate_ExistingDate_ReturnsTransactions() {
        // When
        List<ExternalTransaction> transactions = externalTransactionRepository
                .findByTransactionDate(testDate);

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).allMatch(t -> t.getTransactionDate().equals(testDate));
    }

    @Test
    void findByTransactionDateBetween_DateRange_ReturnsTransactions() {
        // Given
        LocalDate startDate = testDate.minusDays(1);
        LocalDate endDate = testDate.plusDays(1);

        // When
        List<ExternalTransaction> transactions = externalTransactionRepository
                .findByTransactionDateBetween(startDate, endDate);

        // Then
        assertThat(transactions).hasSize(2);
    }

    @Test
    void findByReferenceId_ExistingReference_ReturnsTransaction() {
        // When
        Optional<ExternalTransaction> result = externalTransactionRepository
                .findByReferenceId("REF001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getReferenceId()).isEqualTo("REF001");
        assertThat(result.get().getExternalTransactionId()).isEqualTo("EXT001");
    }

    @Test
    void findByTransactionDateAndStatus_FilteredResults_ReturnsMatchingTransactions() {
        // When
        List<ExternalTransaction> pendingTransactions = externalTransactionRepository
                .findByTransactionDateAndStatus(testDate, ExternalTransaction.ProcessingStatus.PENDING);

        // Then
        assertThat(pendingTransactions).hasSize(1);
        assertThat(pendingTransactions.get(0).getStatus()).isEqualTo(ExternalTransaction.ProcessingStatus.PENDING);
    }

    @Test
    void countByTransactionDate_ReturnsCorrectCount() {
        // When
        long count = externalTransactionRepository.countByTransactionDate(testDate);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void sumAmountByTransactionDate_ReturnsCorrectSum() {
        // When
        BigDecimal sum = externalTransactionRepository.sumAmountByTransactionDate(testDate);

        // Then
        assertThat(sum).isEqualTo(BigDecimal.valueOf(75.00)); // 50.00 + 25.00
    }

    @Test
    void findByFileName_ReturnsTransactionsFromSameFile() {
        // When
        List<ExternalTransaction> transactions = externalTransactionRepository
                .findByFileName("test_file.csv");

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).allMatch(t -> "test_file.csv".equals(t.getFileName()));
    }

    @Test
    void existsByReferenceIdAndTransactionDate_ExistingCombination_ReturnsTrue() {
        // When
        boolean exists = externalTransactionRepository
                .existsByReferenceIdAndTransactionDate("REF001", testDate);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void findDistinctTransactionDates_ReturnsUniqueDates() {
        // Given - Add transaction for different date
        LocalDate otherDate = LocalDate.of(2024, 1, 16);
        ExternalTransaction otherTransaction = ExternalTransaction.builder()
                .externalTransactionId("EXT003")
                .transactionDate(otherDate)
                .amount(BigDecimal.valueOf(30.00))
                .referenceId("REF003")
                .providerName("TEST_PROVIDER")
                .build();
        externalTransactionRepository.save(otherTransaction);

        // When
        List<LocalDate> distinctDates = externalTransactionRepository.findDistinctTransactionDates();

        // Then
        assertThat(distinctDates).hasSize(2);
        assertThat(distinctDates).containsExactlyInAnyOrder(testDate, otherDate);
    }
}