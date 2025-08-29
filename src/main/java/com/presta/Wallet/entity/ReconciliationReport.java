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
import java.util.List;

@Entity
@Table(name = "reconciliation_reports",
       indexes = {
           @Index(name = "idx_reconciliation_date", columnList = "reconciliation_date"),
           @Index(name = "idx_reconciliation_status", columnList = "status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;
    
    @Column(name = "total_internal_transactions", nullable = false)
    private Integer totalInternalTransactions;
    
    @Column(name = "total_external_transactions", nullable = false)
    private Integer totalExternalTransactions;
    
    @Column(name = "matched_transactions", nullable = false)
    private Integer matchedTransactions;
    
    @Column(name = "unmatched_internal", nullable = false)
    private Integer unmatchedInternal;
    
    @Column(name = "unmatched_external", nullable = false)
    private Integer unmatchedExternal;
    
    @Column(name = "amount_differences", nullable = false)
    private Integer amountDifferences;
    
    @Column(name = "total_internal_amount", precision = 19, scale = 2)
    private BigDecimal totalInternalAmount;
    
    @Column(name = "total_external_amount", precision = 19, scale = 2)
    private BigDecimal totalExternalAmount;
    
    @Column(name = "difference_amount", precision = 19, scale = 2)
    private BigDecimal differenceAmount;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReconciliationStatus status = ReconciliationStatus.IN_PROGRESS;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "provider_name")
    private String providerName;
    
    @OneToMany(mappedBy = "reconciliationReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReconciliationItem> items;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public enum ReconciliationStatus {
        IN_PROGRESS, COMPLETED, FAILED
    }
    
    public void markCompleted() {
        this.status = ReconciliationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}

