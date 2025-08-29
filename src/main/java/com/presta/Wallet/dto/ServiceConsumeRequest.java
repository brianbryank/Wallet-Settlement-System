package com.presta.Wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConsumeRequest {
    
    @NotBlank(message = "Service type is required")
    private String serviceType; // CRB, KYC, CREDIT_SCORING
    
    @NotBlank(message = "Reference ID is required")
    private String referenceId;
    
    private String description;
    
    private String externalReference;
    
    // the Service-specific parameters
    private String customerId;
    private String nationalId;
    private String phoneNumber;
}