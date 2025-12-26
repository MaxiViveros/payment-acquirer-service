package com.acquirer.exception;

/**
 * Exception thrown when merchant is not found or inactive
 */
public class MerchantNotFoundException extends RuntimeException {
    
    public MerchantNotFoundException(String message) {
        super(message);
    }
}
