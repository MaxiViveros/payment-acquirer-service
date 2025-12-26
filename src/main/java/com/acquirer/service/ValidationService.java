package com.acquirer.service;

import com.acquirer.entity.Merchant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ValidationService {

    @Value("${acquirer.rules.max-amount-per-merchant:10000.00}")
    private BigDecimal defaultMaxAmount;

    @Value("${acquirer.rules.blocked-card-patterns}")
    private String blockedCardPatterns;

    /**
     * Validate amount against merchant limits
     * 
     * @param amount Transaction amount
     * @param merchant Merchant entity
     * @return Validation result with reason if invalid
     */
    public ValidationResult validateAmount(BigDecimal amount, Merchant merchant) {
        BigDecimal maxAmount = merchant.getMaxTransactionAmount() != null 
                ? merchant.getMaxTransactionAmount() 
                : defaultMaxAmount;

        if (amount.compareTo(maxAmount) > 0) {
            String reason = String.format("Amount %.2f exceeds merchant limit %.2f", 
                                        amount, maxAmount);
            log.warn("Amount validation failed for merchant {}: {}", 
                    merchant.getMerchantId(), reason);
            return ValidationResult.invalid(reason);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount for merchant {}: {}", merchant.getMerchantId(), amount);
            return ValidationResult.invalid("Amount must be greater than zero");
        }

        return ValidationResult.valid();
    }

    /**
     * Validate card token against blocked patterns
     * 
     * @param cardToken Card token or hash
     * @return Validation result with reason if invalid
     */
    public ValidationResult validateCardToken(String cardToken) {
        if (cardToken == null || cardToken.trim().isEmpty()) {
            return ValidationResult.invalid("Card token is required");
        }

        List<String> patterns = Arrays.asList(blockedCardPatterns.split(","));
        
        for (String pattern : patterns) {
            if (cardToken.matches(pattern.trim())) {
                log.warn("Card token matches blocked pattern: {}", maskCardToken(cardToken));
                return ValidationResult.invalid("Card is blocked");
            }
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateCurrency(String currency) {
        List<String> supportedCurrencies = Arrays.asList("USD", "EUR", "GBP", "ARS", "BRL");
        
        if (!supportedCurrencies.contains(currency.toUpperCase())) {
            log.warn("Unsupported currency: {}", currency);
            return ValidationResult.invalid("Currency not supported: " + currency);
        }

        return ValidationResult.valid();
    }

    private String maskCardToken(String cardToken) {
        if (cardToken == null || cardToken.length() < 4) {
            return "****";
        }
        return "**** " + cardToken.substring(cardToken.length() - 4);
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String reason;

        private ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }
    }
}
