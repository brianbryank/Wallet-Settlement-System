package com.presta.Wallet.mapper;


import org.springframework.stereotype.Component;

import com.presta.Wallet.dto.TransactionResponse;
import com.presta.Wallet.dto.WalletTransactionDTO;
import com.presta.Wallet.entity.WalletTransaction;

@Component
public class WalletTransactionMapper {

    //this  Map to TransactionResponse (for API responses)
    public TransactionResponse toDTO(WalletTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .serviceType(transaction.getServiceType())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }


    public WalletTransactionDTO toWalletTransactionDTO(WalletTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return WalletTransactionDTO.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .serviceType(transaction.getServiceType())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}