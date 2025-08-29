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
public class ReconciliationItemResponse {
    private Long itemId;
    private String referenceId;
    private String matchType;
    private String discrepancyType;
    
    private Long internalTransactionId;
    private Long externalTransactionId;
    
    private BigDecimal internalAmount;
    private BigDecimal externalAmount;
    private BigDecimal amountDifference;
    
    private String notes;
    private String severity; // HI, Md, LW
    
    public String getSeverity() {
        if (discrepancyType == null) {
            return "LOW";
        }
        
        return switch (discrepancyType) {
            case "MISSING_INTERNAL", "MISSING_EXTERNAL" -> "HIGH";
            case "AMOUNT_DIFFERENCE" -> {
                if (amountDifference != null && amountDifference.abs().compareTo(BigDecimal.valueOf(100)) > 0) {
                    yield "HIGH";
                } else if (amountDifference != null && amountDifference.abs().compareTo(BigDecimal.valueOf(10)) > 0) {
                    yield "MEDIUM";
                } else {
                    yield "LOW";
                }
            }
            case "DUPLICATE_REFERENCE" -> "MEDIUM";
            default -> "LOW";
        };
    }
}
