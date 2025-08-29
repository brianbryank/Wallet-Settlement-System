package com.presta.Wallet.exception;


public class CustomerNotFoundException extends WalletException {
    public CustomerNotFoundException(String message) {
        super(message, "CUSTOMER_NOT_FOUND");
    }
    
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with ID: " + customerId, "CUSTOMER_NOT_FOUND");
    }
}
