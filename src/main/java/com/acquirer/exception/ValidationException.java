package com.acquirer.exception;

/**
 * Exception thrown when business rule validation fails
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
}
