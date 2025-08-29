package com.presta.Wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationSummary {
    private Integer totalInternalTransactions;
    private Integer totalExternalTransactions;
    private Integer matchedTransactions;
    private Integer unmatchedInternal;
    private Integer unmatchedExternal;
    private Integer amountDifferences;
    
    private BigDecimal totalInternalAmount;
    private BigDecimal totalExternalAmount;
    private BigDecimal differenceAmount;
    
    private Double matchPercentage;
    private String reconciliationStatus;
    
    // the Computed properties
    public Double getMatchPercentage() {
        if (totalInternalTransactions == 0 && totalExternalTransactions == 0) {
            return 100.0;
        }
        int totalTransactions = Math.max(totalInternalTransactions, totalExternalTransactions);
        return (matchedTransactions.doubleValue() / totalTransactions) * 100.0;
    }
    
    public String getReconciliationStatus() {
        double matchPct = getMatchPercentage();
        if (matchPct == 100.0) {
            return "PERFECT";
        } else if (matchPct >= 95.0) {
            return "GOOD";
        } else if (matchPct >= 85.0) {
            return "ACCEPTABLE";
        } else {
            return "POOR";
        }
    }
}