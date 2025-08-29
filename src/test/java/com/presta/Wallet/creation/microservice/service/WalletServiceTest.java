package com.presta.Wallet.creation.microservice.service; 

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.presta.Wallet.dto.BalanceResponse;
import com.presta.Wallet.dto.CreateWalletRequest;
import com.presta.Wallet.dto.WalletDTO;
import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.entity.Wallet;
import com.presta.Wallet.exception.DuplicateResourceException;
import com.presta.Wallet.exception.WalletNotFoundException;
import com.presta.Wallet.mapper.WalletMapper;
import com.presta.Wallet.repository.WalletRepository;
import com.presta.Wallet.service.CustomerService;
import com.presta.Wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private WalletService walletService;

    private Customer customer;
    private Wallet wallet;
    private WalletDTO walletDTO;
    private CreateWalletRequest createRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        wallet = Wallet.builder()
                .id(1L)
                .customer(customer)
                .balance(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(Wallet.WalletStatus.ACTIVE)
                .walletType(Wallet.WalletType.CREDITS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        walletDTO = WalletDTO.builder()
                .id(1L)
                .customerId(1L)
                .customerName("John Doe")
                .balance(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status("ACTIVE")
                .walletType("CREDITS")
                .build();

        createRequest = CreateWalletRequest.builder()
                .customerId(1L)
                .currency("USD")
                .walletType("CREDITS")
                .build();
    }

    @Test
    void createWallet_Success() {
        // Given
        when(customerService.getCustomerEntityById(1L)).thenReturn(customer);
        when(walletRepository.existsByCustomerIdAndWalletType(1L, Wallet.WalletType.CREDITS)).thenReturn(false);
        when(walletMapper.toEntity(any(CreateWalletRequest.class), any(Customer.class))).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletMapper.toDTO(any(Wallet.class))).thenReturn(walletDTO);

        // When
        WalletDTO result = walletService.createWallet(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(100.00));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_DuplicateWalletType_ThrowsException() {
        // Given
        when(customerService.getCustomerEntityById(1L)).thenReturn(customer);
        when(walletRepository.existsByCustomerIdAndWalletType(1L, Wallet.WalletType.CREDITS)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> walletService.createWallet(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Wallet of type CREDITS already exists");
    }

    @Test
    void getWalletBalance_Success() {
        // Given
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        // When
        BalanceResponse result = walletService.getWalletBalance(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWalletId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void getWalletById_NotFound_ThrowsException() {
        // Given
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> walletService.getWalletBalance(1L))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("Wallet not found with ID: 1");
    }

    @Test
    void getWalletsByCustomerId_Success() {
        // Given
        List<Wallet> wallets = Arrays.asList(wallet);
        when(customerService.getCustomerEntityById(1L)).thenReturn(customer);
        when(walletRepository.findByCustomerId(1L)).thenReturn(wallets);
        when(walletMapper.toDTO(wallet)).thenReturn(walletDTO);

        // When
        List<WalletDTO> result = walletService.getWalletsByCustomerId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void saveWallet_Success() {
        // Given
        when(walletRepository.save(wallet)).thenReturn(wallet);

        // When
        Wallet result = walletService.saveWallet(wallet);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(walletRepository).save(wallet);
    }
}
