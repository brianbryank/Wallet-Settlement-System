package com.presta.Wallet.exception;

public class WalletException extends RuntimeException {
    private final String errorCode;

    public WalletException(String message) {
        super(message);
        this.errorCode = null;
    }

    public WalletException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public WalletException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
