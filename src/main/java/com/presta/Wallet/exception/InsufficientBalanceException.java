package com.presta.Wallet.exception;

import java.math.BigDecimal;
//No funds please topup
public class InsufficientBalanceException extends WalletException {
    public InsufficientBalanceException(String message) {
        super(message, "INSUFFICIENT_BALANCE");
    }
    
    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance. Required: %s, Available: %s", required, available), 
              "INSUFFICIENT_BALANCE");
    }
}
