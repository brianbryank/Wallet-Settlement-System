package com.presta.Wallet.creation.microservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.presta.Wallet.ServiceSimulator;
import com.presta.Wallet.dto.ConsumeRequest;
import com.presta.Wallet.dto.ServiceConsumeRequest;
import com.presta.Wallet.dto.ServiceResponse;
import com.presta.Wallet.dto.TopupRequest;
import com.presta.Wallet.dto.TransactionResponse;
import com.presta.Wallet.entity.Customer;
import com.presta.Wallet.entity.Wallet;
import com.presta.Wallet.entity.WalletTransaction;
import com.presta.Wallet.exception.InsufficientBalanceException;
import com.presta.Wallet.exception.WalletException;
import com.presta.Wallet.mapper.WalletTransactionMapper;
import com.presta.Wallet.repository.WalletTransactionRepository;
import com.presta.Wallet.service.NotificationService;
import com.presta.Wallet.service.TransactionService;
import com.presta.Wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private WalletService walletService;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @Mock
    private WalletTransactionMapper transactionMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ServiceSimulator serviceSimulator;

    @InjectMocks
    private TransactionService transactionService;

    private Customer customer;
    private Wallet wallet;
    private WalletTransaction transaction;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        wallet = Wallet.builder()
                .id(1L)
                .customer(customer)
                .balance(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(Wallet.WalletStatus.ACTIVE)
                .walletType(Wallet.WalletType.CREDITS)
                .build();

        transaction = WalletTransaction.builder()
                .id(1L)
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.TOPUP)
                .amount(BigDecimal.valueOf(50.00))
                .referenceId("REF123")
                .status(WalletTransaction.TransactionStatus.COMPLETED)
                .balanceBefore(BigDecimal.valueOf(100.00))
                .balanceAfter(BigDecimal.valueOf(150.00))
                .createdAt(LocalDateTime.now())
                .build();

        transactionResponse = TransactionResponse.builder()
                .transactionId(1L)
                .walletId(1L)
                .transactionType("TOPUP")
                .amount(BigDecimal.valueOf(50.00))
                .referenceId("REF123")
                .status("COMPLETED")
                .balanceBefore(BigDecimal.valueOf(100.00))
                .balanceAfter(BigDecimal.valueOf(150.00))
                .build();
    }

    @Test
    void topup_Success() {
        // Given
        TopupRequest request = TopupRequest.builder()
                .amount(BigDecimal.valueOf(50.00))
                .referenceId("REF123")
                .description("Test topup")
                .build();

        when(transactionRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(walletService.getWalletEntityById(1L)).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletService.saveWallet(any(Wallet.class))).thenReturn(wallet);
        when(transactionMapper.toDTO(any(WalletTransaction.class))).thenReturn(transactionResponse);

        // When
        TransactionResponse result = transactionService.topup(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionType()).isEqualTo("TOPUP");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(50.00));
        verify(transactionRepository, times(2)).save(any(WalletTransaction.class));
        verify(walletService).saveWallet(wallet);
        verify(notificationService).publishTransactionEvent(any());
    }

    @Test
    void topup_DuplicateReference_ThrowsException() {
        // Given
        TopupRequest request = TopupRequest.builder()
                .amount(BigDecimal.valueOf(50.00))
                .referenceId("REF123")
                .build();

        when(transactionRepository.existsByReferenceId("REF123")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transactionService.topup(1L, request))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Duplicate transaction reference");
    }

    @Test
    void consume_Success() {
        // Given
        ConsumeRequest request = ConsumeRequest.builder()
                .amount(BigDecimal.valueOf(30.00))
                .referenceId("REF124")
                .serviceType("CRB")
                .description("Test consumption")
                .build();

        when(transactionRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(walletService.getWalletEntityById(1L)).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletService.saveWallet(any(Wallet.class))).thenReturn(wallet);
        when(transactionMapper.toDTO(any(WalletTransaction.class))).thenReturn(transactionResponse);

        // When
        TransactionResponse result = transactionService.consume(1L, request);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository, times(2)).save(any(WalletTransaction.class));
        verify(walletService).saveWallet(wallet);
        verify(notificationService).publishTransactionEvent(any());
    }

    @Test
    void consume_InsufficientBalance_ThrowsException() {
        // Given
        ConsumeRequest request = ConsumeRequest.builder()
                .amount(BigDecimal.valueOf(200.00))
                .referenceId("REF125")
                .serviceType("CRB")
                .build();

        wallet.setBalance(BigDecimal.valueOf(50.00));

        when(transactionRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(walletService.getWalletEntityById(1L)).thenReturn(wallet);

        // When & Then
        assertThatThrownBy(() -> transactionService.consume(1L, request))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void consumeService_Success() {
        // Given
        ServiceConsumeRequest request = ServiceConsumeRequest.builder()
                .serviceType("KYC")
                .referenceId("KYC_001")
                .customerId("CUST_123")
                .nationalId("12345678")
                .phoneNumber("+254700000000")
                .build();

        ServiceResponse serviceResponse = ServiceResponse.builder()
                .serviceType("KYC")
                .status("SUCCESS")
                .cost(BigDecimal.valueOf(25.00))
                .build();

        when(serviceSimulator.getServiceCost("KYC")).thenReturn(BigDecimal.valueOf(25.00));
        when(transactionRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(walletService.getWalletEntityById(1L)).thenReturn(wallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(transaction);
        when(walletService.saveWallet(any(Wallet.class))).thenReturn(wallet);
        when(transactionMapper.toDTO(any(WalletTransaction.class))).thenReturn(transactionResponse);
        when(serviceSimulator.callKYCService(anyString(), anyString(), anyString())).thenReturn(serviceResponse);

        // When
        ServiceResponse result = transactionService.consumeService(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getServiceType()).isEqualTo("KYC");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        verify(serviceSimulator).callKYCService(anyString(), anyString(), anyString());
    }

    @Test
    void getTransactionByReference_Success() {
        // Given
        when(transactionRepository.findByReferenceId("REF123")).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDTO(transaction)).thenReturn(transactionResponse);

        // When
        TransactionResponse result = transactionService.getTransactionByReference("REF123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReferenceId()).isEqualTo("REF123");
        verify(transactionRepository).findByReferenceId("REF123");
    }

    @Test
    void getTransactionByReference_NotFound_ThrowsException() {
        // Given
        when(transactionRepository.findByReferenceId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionByReference("INVALID"))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("Transaction not found");
    }
}
