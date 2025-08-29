package com.presta.Wallet.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.presta.Wallet.ServiceSimulator;
import com.presta.Wallet.config.TransactionMessage;
import com.presta.Wallet.dto.ConsumeRequest;
import com.presta.Wallet.dto.ServiceConsumeRequest;
import com.presta.Wallet.dto.ServiceResponse;
import com.presta.Wallet.dto.TopupRequest;
import com.presta.Wallet.dto.TransactionHistoryRequest;
import com.presta.Wallet.dto.TransactionResponse;
import com.presta.Wallet.entity.Wallet;
import com.presta.Wallet.entity.WalletTransaction;
import com.presta.Wallet.exception.InsufficientBalanceException;
import com.presta.Wallet.exception.WalletException;
import com.presta.Wallet.mapper.WalletTransactionMapper;
import com.presta.Wallet.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletService walletService;
    private final WalletTransactionRepository transactionRepository;
    private final WalletTransactionMapper transactionMapper;
    private final NotificationService notificationService;
    private final ServiceSimulator serviceSimulator;

    @Transactional
    public TransactionResponse topup(Long walletId, TopupRequest request) {
        log.info("Processing top-up for wallet: {}, amount: {}, reference: {}", 
                walletId, request.getAmount(), request.getReferenceId());

        //this  Check for duplicate transaction
        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            throw new WalletException("Duplicate transaction reference: " + request.getReferenceId(), "DUPLICATE_TRANSACTION");
        }

        // Get my wallet
        Wallet wallet = walletService.getWalletEntityById(walletId);
        BigDecimal balanceBefore = wallet.getBalance();

        // Create my transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.TOPUP)
                .amount(request.getAmount())
                .referenceId(request.getReferenceId())
                .description(request.getDescription() != null ? request.getDescription() : "Wallet top-up")
                .status(WalletTransaction.TransactionStatus.PENDING)
                .balanceBefore(balanceBefore)
                .serviceType(request.getSource())
                .build();

        try {
            // Credit my wallet balance
            wallet.credit(request.getAmount());
            transaction.setBalanceAfter(wallet.getBalance());
            
        
            WalletTransaction savedTransaction = transactionRepository.save(transaction);
            walletService.saveWallet(wallet); // We need to add this method

            // done
            savedTransaction.markCompleted();
            savedTransaction = transactionRepository.save(savedTransaction);

            log.info("Top-up completed successfully for wallet: {}, new balance: {}", 
                    walletId, wallet.getBalance());

            // Sending to queue asynchronously
            notificationService.publishTransactionEvent(createTransactionMessage(savedTransaction));

            return transactionMapper.toDTO(savedTransaction);

        } catch (Exception e) {
            log.error("Top-up failed for wallet: {}, reference: {}", walletId, request.getReferenceId(), e);
            transaction.markFailed();
            transactionRepository.save(transaction);
            throw new WalletException("Top-up operation failed: " + e.getMessage());
        }
    }

    @Transactional
    public TransactionResponse consume(Long walletId, ConsumeRequest request) {
        log.info("Processing consumption for wallet: {}, amount: {}, service: {}, reference: {}", 
                walletId, request.getAmount(), request.getServiceType(), request.getReferenceId());

        // Check for duplicate of transaction
        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            throw new WalletException("Duplicate transaction reference: " + request.getReferenceId(), "DUPLICATE_TRANSACTION");
        }

        Wallet wallet = walletService.getWalletEntityById(walletId);
        BigDecimal balanceBefore = wallet.getBalance();

        // Check sufficient balance
        if (!wallet.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException(request.getAmount(), wallet.getBalance());
        }

    
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.CONSUMPTION)
                .amount(request.getAmount())
                .referenceId(request.getReferenceId())
                .description(request.getDescription() != null ? request.getDescription() : 
                           "Service consumption: " + request.getServiceType())
                .status(WalletTransaction.TransactionStatus.PENDING)
                .balanceBefore(balanceBefore)
                .serviceType(request.getServiceType())
                .build();

        try {
            // Debit wallet balance
            wallet.debit(request.getAmount());
            transaction.setBalanceAfter(wallet.getBalance());
            
        
            WalletTransaction savedTransaction = transactionRepository.save(transaction);
            walletService.saveWallet(wallet);

            // Mark completed
            savedTransaction.markCompleted();
            savedTransaction = transactionRepository.save(savedTransaction);

            log.info("Consumption completed successfully for wallet: {}, new balance: {}", 
                    walletId, wallet.getBalance());

            // Send to queue asynchronously
            notificationService.publishTransactionEvent(createTransactionMessage(savedTransaction));

            return transactionMapper.toDTO(savedTransaction);

        } catch (Exception e) {
            log.error("Consumption failed for wallet: {}, reference: {}", walletId, request.getReferenceId(), e);
            transaction.markFailed();
            transactionRepository.save(transaction);
            throw new WalletException("Consumption operation failed: " + e.getMessage());
        }
    }

    @Transactional
    public ServiceResponse consumeService(Long walletId, ServiceConsumeRequest request) {
        log.info("Processing service consumption for wallet: {}, service: {}, reference: {}", 
                walletId, request.getServiceType(), request.getReferenceId());

        // lets get the service cost
        BigDecimal serviceCost = serviceSimulator.getServiceCost(request.getServiceType());

        // Create consumption request
        ConsumeRequest consumeRequest = ConsumeRequest.builder()
                .amount(serviceCost)
                .referenceId(request.getReferenceId())
                .serviceType(request.getServiceType())
                .description("Service consumption: " + request.getServiceType())
                .externalReference(request.getExternalReference())
                .build();

        try {
            // Deduct from my wallet first
            TransactionResponse transaction = consume(walletId, consumeRequest);

            // Call external service
            ServiceResponse serviceResponse = callExternalService(request);

            log.info("Service consumption completed: wallet: {}, service: {}, status: {}", 
                    walletId, request.getServiceType(), serviceResponse.getStatus());

            return serviceResponse;

        } catch (Exception e) {
            log.error("Service consumption failed: wallet: {}, service: {}", walletId, request.getServiceType(), e);
            throw new WalletException("Service consumption failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long walletId, TransactionHistoryRequest request) {
        log.info("Fetching transaction history for wallet: {}", walletId);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<WalletTransaction> transactions;

        if (request.getStartDate() != null && request.getEndDate() != null) {
            transactions = transactionRepository.findByWalletIdAndDateRange(
                    walletId, request.getStartDate(), request.getEndDate());
        } else {
            Page<WalletTransaction> page = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
            transactions = page.getContent();
        }

        return transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceId) {
        log.info("Fetching transaction by reference: {}", referenceId);

        WalletTransaction transaction = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new WalletException("Transaction not found with reference: " + referenceId, 
                                                      "TRANSACTION_NOT_FOUND"));

        return transactionMapper.toDTO(transaction);
    }

    private ServiceResponse callExternalService(ServiceConsumeRequest request) {
        return switch (request.getServiceType().toUpperCase()) {
            case "CRB" -> serviceSimulator.callCRBService(
                    request.getCustomerId(), 
                    request.getPhoneNumber(), 
                    request.getReferenceId());
            case "KYC" -> serviceSimulator.callKYCService(
                    request.getNationalId(), 
                    request.getPhoneNumber(), 
                    request.getReferenceId());
            case "CREDIT_SCORING" -> serviceSimulator.callCreditScoringService(
                    request.getNationalId(), 
                    request.getPhoneNumber(), 
                    request.getReferenceId());
            default -> throw new WalletException("Unknown service type: " + request.getServiceType(), 
                                                "UNKNOWN_SERVICE_TYPE");
        };
    }

    private TransactionMessage createTransactionMessage(WalletTransaction transaction) {
        return TransactionMessage.builder()
                .transactionId(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .customerId(transaction.getWallet().getCustomer().getId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .serviceType(transaction.getServiceType())
                .status(transaction.getStatus().name())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .timestamp(transaction.getCreatedAt())
                .description(transaction.getDescription())
                .messageId(UUID.randomUUID().toString())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }
}