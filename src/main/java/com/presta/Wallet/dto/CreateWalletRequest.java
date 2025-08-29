package com.presta.Wallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private String currency = "KSH";
    private String walletType = "CREDITS";
}
