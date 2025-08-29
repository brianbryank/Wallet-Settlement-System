package com.presta.Wallet.creation.microservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.entity.Wallet;
import com.presta.Wallet.repository.CustomerRepository;
import com.presta.Wallet.repository.WalletRepository;

@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private Wallet creditsWallet;
    private Wallet cashWallet;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        customerRepository.deleteAll();

        customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phoneNumber("+254700123456")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();
        customer = customerRepository.save(customer);

        creditsWallet = Wallet.builder()
                .customer(customer)
                .balance(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(Wallet.WalletStatus.ACTIVE)
                .walletType(Wallet.WalletType.CREDITS)
                .build();

        cashWallet = Wallet.builder()
                .customer(customer)
                .balance(BigDecimal.valueOf(50.00))
                .currency("USD")
                .status(Wallet.WalletStatus.ACTIVE)
                .walletType(Wallet.WalletType.CASH)
                .build();

        walletRepository.save(creditsWallet);
        walletRepository.save(cashWallet);
    }

    @Test
    void findByCustomerId_ExistingCustomer_ReturnsWallets() {
        // When
        List<Wallet> wallets = walletRepository.findByCustomerId(customer.getId());

        // Then
        assertThat(wallets).hasSize(2);
        assertThat(wallets).extracting(Wallet::getWalletType)
                .containsExactlyInAnyOrder(Wallet.WalletType.CREDITS, Wallet.WalletType.CASH);
    }

    @Test
    void findByCustomerIdAndStatus_ActiveWallets_ReturnsActiveWallets() {
        // When
        List<Wallet> activeWallets = walletRepository.findByCustomerIdAndStatus(
                customer.getId(), Wallet.WalletStatus.ACTIVE);

        // Then
        assertThat(activeWallets).hasSize(2);
        assertThat(activeWallets).allMatch(w -> w.getStatus() == Wallet.WalletStatus.ACTIVE);
    }

    @Test
    void findByCustomerIdAndWalletType_CreditsWallet_ReturnsCreditsWallet() {
        // When
        Optional<Wallet> wallet = walletRepository.findByCustomerIdAndWalletType(
                customer.getId(), Wallet.WalletType.CREDITS);

        // Then
        assertThat(wallet).isPresent();
        assertThat(wallet.get().getWalletType()).isEqualTo(Wallet.WalletType.CREDITS);
        assertThat(wallet.get().getBalance()).isEqualTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void existsByCustomerIdAndWalletType_ExistingWallet_ReturnsTrue() {
        // When
        boolean exists = walletRepository.existsByCustomerIdAndWalletType(
                customer.getId(), Wallet.WalletType.CREDITS);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCustomerIdAndWalletType_NonExistingWallet_ReturnsFalse() {
        // When
        boolean exists = walletRepository.existsByCustomerIdAndWalletType(
                customer.getId(), Wallet.WalletType.POINTS);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_NewWallet_PersistsWithVersion() {
        // Given
        Wallet newWallet = Wallet.builder()
                .customer(customer)
                .balance(BigDecimal.valueOf(75.00))
                .currency("USD")
                .status(Wallet.WalletStatus.ACTIVE)
                .walletType(Wallet.WalletType.POINTS)
                .build();

        // When
        Wallet saved = walletRepository.save(newWallet);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
