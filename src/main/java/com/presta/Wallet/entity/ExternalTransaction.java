package com.presta.Wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_transactions", 
       indexes = {
           @Index(name = "idx_external_transaction_id", columnList = "external_transaction_id"),
           @Index(name = "idx_external_transaction_date", columnList = "transaction_date"),
           @Index(name = "idx_external_reference_id", columnList = "reference_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "external_transaction_id", nullable = false)
    private String externalTransactionId;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Column(name = "transaction_type")
    private String transactionType; // CREDIT, DEBIT, etc.
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "service_type")
    private String serviceType;
    
    private String description;
    
    @Column(name = "provider_name")
    private String providerName; // External system name
    
    @Column(name = "file_name")
    private String fileName; // Source file name
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    public enum ProcessingStatus {
        PENDING, PROCESSED, FAILED
    }
}
