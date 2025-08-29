package com.presta.Wallet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", 
       indexes = {
           @Index(name = "idx_wallet_id", columnList = "wallet_id"),
           @Index(name = "idx_reference_id", columnList = "reference_id"),
           @Index(name = "idx_transaction_type", columnList = "transaction_type"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    @NotNull(message = "Wallet is required")
    private Wallet wallet;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Column(precision = 19, scale = 2, nullable = false)
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @Column(name = "reference_id", unique = true, nullable = false)
    @NotNull(message = "Reference ID is required")
    private String referenceId;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "balance_before", precision = 19, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(name = "service_type")
    private String serviceType; //maybe  CRB, KYC, CREDIT_SCORING, etc.
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    public enum TransactionType {
        CREDIT, DEBIT, TOPUP, CONSUMPTION, REVERSAL, ADJUSTMENT
    }
    
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED, REVERSED
    }
    

    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markFailed() {
        this.status = TransactionStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
    
    public boolean isCredit() {
        return transactionType == TransactionType.CREDIT || 
               transactionType == TransactionType.TOPUP;
    }
    
    public boolean isDebit() {
        return transactionType == TransactionType.DEBIT || 
               transactionType == TransactionType.CONSUMPTION;
    }
}
