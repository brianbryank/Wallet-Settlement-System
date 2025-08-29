package com.presta.Wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryRequest {
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    private String transactionType; // CREDIT, DEBIT, TOPUP or  CONSUMPTION
    private String status; // check if it, PENDING, COMPLETED or FAILED
    private String serviceType; // CRB, KYC, CREDIT_SCORING
    
    private int page = 0;
    private int size = 20;
}
