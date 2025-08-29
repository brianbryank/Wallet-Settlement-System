package com.presta.Wallet.exception;

public class WalletNotFoundException extends WalletException {
    public WalletNotFoundException(String message) {
        super(message, "WALLET_NOT_FOUND");
    }
    
    public WalletNotFoundException(Long walletId) {
        super("Wallet not found with ID: " + walletId, "WALLET_NOT_FOUND");
    }
}
