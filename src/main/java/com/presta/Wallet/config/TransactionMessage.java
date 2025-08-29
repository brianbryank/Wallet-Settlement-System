package com.presta.Wallet.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMessage {
    private Long transactionId;
    private Long walletId;
    private Long customerId;
    private String transactionType;
    private BigDecimal amount;
    private String referenceId;
    private String serviceType;
    private String status;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime timestamp;
    private String description;
    private String source;
    private String externalReference;
    
    // this is mess...metadata
    private String messageId;
    private int retryCount;
    private LocalDateTime createdAt;
}

