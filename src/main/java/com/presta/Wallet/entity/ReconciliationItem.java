package com.presta.Wallet.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "reconciliation_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_report_id", nullable = false)
    private ReconciliationReport reconciliationReport;
    
    @Column(name = "internal_transaction_id")
    private Long internalTransactionId;
    
    @Column(name = "external_transaction_id")
    private Long externalTransactionId;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Enumerated(EnumType.STRING)
    private MatchType matchType;
    
    @Enumerated(EnumType.STRING)
    private DiscrepancyType discrepancyType;
    
    @Column(name = "internal_amount", precision = 19, scale = 2)
    private BigDecimal internalAmount;
    
    @Column(name = "external_amount", precision = 19, scale = 2)
    private BigDecimal externalAmount;
    
    @Column(name = "amount_difference", precision = 19, scale = 2)
    private BigDecimal amountDifference;
    
    private String notes;
    
    public enum MatchType {
        PERFECT_MATCH,      // Exact match on reference and amount
        REFERENCE_MATCH,    // Reference matches but amount differs
        AMOUNT_MATCH,       // Amount matches but reference differs
        NO_MATCH           // No corresponding transaction found
    }
    
    public enum DiscrepancyType {
        NONE,                    // Perfect match
        AMOUNT_DIFFERENCE,       // Different amounts
        MISSING_INTERNAL,        // External transaction with no internal match
        MISSING_EXTERNAL,        // Internal transaction with no external match
        DUPLICATE_REFERENCE     // Same reference used multiple times
    }
}