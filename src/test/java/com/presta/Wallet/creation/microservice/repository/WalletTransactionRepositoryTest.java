package com.presta.Wallet.creation.microservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.entity.Wallet;
import com.presta.Wallet.entity.WalletTransaction;
import com.presta.Wallet.repository.CustomerRepository;
import com.presta.Wallet.repository.WalletRepository;
import com.presta.Wallet.repository.WalletTransactionRepository;

@DataJpaTest
@ActiveProfiles("test")
class WalletTransactionRepositoryTest {

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Wallet wallet;
    private WalletTransaction transaction1;
    private WalletTransaction transaction2;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();
        customer = customerRepository.save(customer);

        wallet = Wallet.builder()
                .customer(customer)
                .balance(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(Wallet.WalletStatus.ACTIVE)
                .walletType(Wallet.WalletType.CREDITS)
                .build();
        wallet = walletRepository.save(wallet);

        transaction1 = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.TOPUP)
                .amount(BigDecimal.valueOf(50.00))
                .referenceId("REF001")
                .status(WalletTransaction.TransactionStatus.COMPLETED)
                .balanceBefore(BigDecimal.valueOf(50.00))
                .balanceAfter(BigDecimal.valueOf(100.00))
                .serviceType("BANK_TRANSFER")
                .build();

        transaction2 = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.CONSUMPTION)
                .amount(BigDecimal.valueOf(25.00))
                .referenceId("REF002")
                .status(WalletTransaction.TransactionStatus.COMPLETED)
                .balanceBefore(BigDecimal.valueOf(100.00))
                .balanceAfter(BigDecimal.valueOf(75.00))
                .serviceType("CRB")
                .build();

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
    }

    @Test
    void findByReferenceId_ExistingReference_ReturnsTransaction() {
        // When
        Optional<WalletTransaction> result = transactionRepository.findByReferenceId("REF001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getReferenceId()).isEqualTo("REF001");
        assertThat(result.get().getTransactionType()).isEqualTo(WalletTransaction.TransactionType.TOPUP);
    }

    @Test
    void existsByReferenceId_ExistingReference_ReturnsTrue() {
        // When
        boolean exists = transactionRepository.existsByReferenceId("REF001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByReferenceId_NonExistingReference_ReturnsFalse() {
        // When
        boolean exists = transactionRepository.existsByReferenceId("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByWalletIdOrderByCreatedAtDesc_ReturnsTransactionsInDescOrder() {
        // When
        List<WalletTransaction> transactions = transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId());

        // Then
        assertThat(transactions).hasSize(2);
        // Verify they are ordered by creation date descending (newest first)
        assertThat(transactions.get(0).getCreatedAt()).isAfterOrEqualTo(transactions.get(1).getCreatedAt());
    }

    @Test
    void findByWalletIdOrderByCreatedAtDesc_WithPageable_ReturnsPagedResults() {
        // When
        Page<WalletTransaction> page = transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), PageRequest.of(0, 1));

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findByWalletIdAndDateRange_WithinRange_ReturnsTransactions() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        // When
        List<WalletTransaction> transactions = transactionRepository
                .findByWalletIdAndDateRange(wallet.getId(), startDate, endDate);

        // Then
        assertThat(transactions).hasSize(2);
    }

    @Test
    void findByStatus_CompletedStatus_ReturnsCompletedTransactions() {
        // When
        List<WalletTransaction> completedTransactions = transactionRepository
                .findByStatus(WalletTransaction.TransactionStatus.COMPLETED);

        // Then
        assertThat(completedTransactions).hasSize(2);
        assertThat(completedTransactions).allMatch(t -> 
            t.getStatus() == WalletTransaction.TransactionStatus.COMPLETED);
    }

    @Test
    void countByWalletIdAndStatus_CompletedTransactions_ReturnsCorrectCount() {
        // When
        long count = transactionRepository.countByWalletIdAndStatus(
                wallet.getId(), WalletTransaction.TransactionStatus.COMPLETED);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void save_NewTransaction_PersistsSuccessfully() {
        // Given
        WalletTransaction newTransaction = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.REVERSAL)
                .amount(BigDecimal.valueOf(10.00))
                .referenceId("REF003")
                .status(WalletTransaction.TransactionStatus.PENDING)
                .build();

        // When
        WalletTransaction saved = transactionRepository.save(newTransaction);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getReferenceId()).isEqualTo("REF003");
    }
}