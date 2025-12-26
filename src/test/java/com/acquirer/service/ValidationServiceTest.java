package com.acquirer.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationService
 */
@SpringBootTest
@TestPropertySource(properties = {
    "acquirer.rules.max-amount-per-merchant=10000.00",
    "acquirer.rules.blocked-card-patterns=^4111111111111111$,^5555555555554444$"
})
class ValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    @Test
    void testValidateCurrency_Supported() {
        // Act
        ValidationService.ValidationResult result = validationService.validateCurrency("USD");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    void testValidateCurrency_Unsupported() {
        // Act
        ValidationService.ValidationResult result = validationService.validateCurrency("XXX");

        // Assert
        assertFalse(result.isValid());
        assertNotNull(result.getReason());
    }

    @Test
    void testValidateCardToken_Valid() {
        // Act
        ValidationService.ValidationResult result = 
                validationService.validateCardToken("tok_1234567890123456");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    void testValidateCardToken_Blocked() {
        // Act
        ValidationService.ValidationResult result = 
                validationService.validateCardToken("4111111111111111");

        // Assert
        assertFalse(result.isValid());
        assertEquals("Card is blocked", result.getReason());
    }

    @Test
    void testValidateCardToken_Empty() {
        // Act
        ValidationService.ValidationResult result = validationService.validateCardToken("");

        // Assert
        assertFalse(result.isValid());
        assertEquals("Card token is required", result.getReason());
    }
}
