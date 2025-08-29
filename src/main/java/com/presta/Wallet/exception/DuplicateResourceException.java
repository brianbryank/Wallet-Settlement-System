package com.presta.Wallet.exception;

public class DuplicateResourceException extends WalletException {
    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}
