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
public class ServiceResponse {
    private String serviceType;
    private String status; // SUCCESS, or FAILED
    private String message;
    private String externalReference;
    private BigDecimal cost;
    private String result; // give Service specific result data
}
