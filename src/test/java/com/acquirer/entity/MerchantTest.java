package com.acquirer.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Merchant Entity Tests")
class MerchantTest {

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = new Merchant();
    }

    @Test
    @DisplayName("Should create merchant with builder")
    void shouldCreateMerchantWithBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        Merchant merchant = Merchant.builder()
                .merchantId("MERCHANT_001")
                .merchantName("Test Merchant")
                .maxTransactionAmount(new BigDecimal("10000.00"))
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(merchant);
        assertEquals("MERCHANT_001", merchant.getMerchantId());
        assertEquals("Test Merchant", merchant.getMerchantName());
        assertEquals(0, merchant.getMaxTransactionAmount().compareTo(new BigDecimal("10000.00")));
        assertTrue(merchant.getActive());
        assertEquals(now, merchant.getCreatedAt());
        assertEquals(now, merchant.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set default values on persist")
    void shouldSetDefaultValuesOnPersist() {
        // Given
        merchant.setMerchantId("MERCHANT_001");
        merchant.setMerchantName("Test Merchant");
        merchant.setMaxTransactionAmount(new BigDecimal("5000.00"));

        // When
        merchant.onCreate();

        // Then
        assertNotNull(merchant.getCreatedAt());
        assertNotNull(merchant.getUpdatedAt());
        assertTrue(merchant.getActive());
    }

    @Test
    @DisplayName("Should not override active flag if already set")
    void shouldNotOverrideActiveFlagIfAlreadySet() {
        // Given
        merchant.setMerchantId("MERCHANT_001");
        merchant.setMerchantName("Test Merchant");
        merchant.setActive(false);

        // When
        merchant.onCreate();

        // Then
        assertFalse(merchant.getActive());
    }

    @Test
    @DisplayName("Should update timestamp on update")
    void shouldUpdateTimestampOnUpdate() {
        // Given
        merchant.setMerchantId("MERCHANT_001");
        merchant.setMerchantName("Test Merchant");
        merchant.onCreate();
        LocalDateTime originalUpdatedAt = merchant.getUpdatedAt();

        // Wait a bit to ensure different timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        merchant.onUpdate();

        // Then
        assertNotNull(merchant.getUpdatedAt());
        assertTrue(merchant.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should allow valid merchant data")
    void shouldAllowValidMerchantData() {
        // Given & When
        merchant.setMerchantId("MERCHANT_123");
        merchant.setMerchantName("Valid Merchant");
        merchant.setMaxTransactionAmount(new BigDecimal("15000.50"));
        merchant.setActive(true);
        merchant.onCreate();

        // Then
        assertEquals("MERCHANT_123", merchant.getMerchantId());
        assertEquals("Valid Merchant", merchant.getMerchantName());
        assertEquals(0, merchant.getMaxTransactionAmount().compareTo(new BigDecimal("15000.50")));
        assertTrue(merchant.getActive());
    }

    @Test
    @DisplayName("Should handle zero max transaction amount")
    void shouldHandleZeroMaxTransactionAmount() {
        // Given & When
        merchant.setMerchantId("MERCHANT_001");
        merchant.setMerchantName("Test Merchant");
        merchant.setMaxTransactionAmount(BigDecimal.ZERO);

        // Then
        assertEquals(0, merchant.getMaxTransactionAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should handle null max transaction amount")
    void shouldHandleNullMaxTransactionAmount() {
        // Given & When
        merchant.setMerchantId("MERCHANT_001");
        merchant.setMerchantName("Test Merchant");
        merchant.setMaxTransactionAmount(null);

        // Then
        assertNull(merchant.getMaxTransactionAmount());
    }

    @Test
    @DisplayName("Should preserve scale in max transaction amount")
    void shouldPreserveScaleInMaxTransactionAmount() {
        // Given
        BigDecimal amount = new BigDecimal("1000.99");

        // When
        merchant.setMaxTransactionAmount(amount);

        // Then
        assertEquals(amount, merchant.getMaxTransactionAmount());
        assertEquals(2, merchant.getMaxTransactionAmount().scale());
    }

    @Test
    @DisplayName("Should support lombok equals and hashCode")
    void shouldSupportLombokEqualsAndHashCode() {
        // Given
        Merchant merchant1 = Merchant.builder()
                .merchantId("MERCHANT_001")
                .merchantName("Test Merchant")
                .maxTransactionAmount(new BigDecimal("5000.00"))
                .active(true)
                .build();

        Merchant merchant2 = Merchant.builder()
                .merchantId("MERCHANT_001")
                .merchantName("Test Merchant")
                .maxTransactionAmount(new BigDecimal("5000.00"))
                .active(true)
                .build();

        // Then
        assertEquals(merchant1, merchant2);
        assertEquals(merchant1.hashCode(), merchant2.hashCode());
    }

    @Test
    @DisplayName("Should support lombok toString")
    void shouldSupportLombokToString() {
        // Given
        merchant.setMerchantId("MERCHANT_001");
        merchant.setMerchantName("Test Merchant");

        // When
        String toString = merchant.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("MERCHANT_001"));
        assertTrue(toString.contains("Test Merchant"));
    }
}
