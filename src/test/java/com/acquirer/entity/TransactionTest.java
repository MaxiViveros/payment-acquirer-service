package com.acquirer.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Entity Tests")
class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
    }

    @Test
    @DisplayName("Should create transaction with builder")
    void shouldCreateTransactionWithBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        String transactionId = "txn-123";

        // When
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .merchantId("MERCHANT_001")
                .amount(new BigDecimal("100.50"))
                .currency("USD")
                .cardToken("tok_1234567890")
                .cardExpiry("12/2025")
                .operationType("PURCHASE")
                .status(Transaction.TransactionStatus.APPROVED)
                .responseCode("00")
                .issuerResponse("APPROVED")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(transaction);
        assertEquals(transactionId, transaction.getTransactionId());
        assertEquals("MERCHANT_001", transaction.getMerchantId());
        assertEquals(0, transaction.getAmount().compareTo(new BigDecimal("100.50")));
        assertEquals("USD", transaction.getCurrency());
        assertEquals("tok_1234567890", transaction.getCardToken());
        assertEquals("12/2025", transaction.getCardExpiry());
        assertEquals("PURCHASE", transaction.getOperationType());
        assertEquals(Transaction.TransactionStatus.APPROVED, transaction.getStatus());
        assertEquals("00", transaction.getResponseCode());
        assertEquals("APPROVED", transaction.getIssuerResponse());
    }

    @Test
    @DisplayName("Should set timestamps on persist")
    void shouldSetTimestampsOnPersist() {
        // Given
        transaction.setMerchantId("MERCHANT_001");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("USD");
        transaction.setCardToken("tok_123");
        transaction.setCardExpiry("12/2025");
        transaction.setOperationType("PURCHASE");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // When
        transaction.onCreate();

        // Then
        assertNotNull(transaction.getCreatedAt());
        assertNotNull(transaction.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update timestamp on update")
    void shouldUpdateTimestampOnUpdate() {
        // Given
        transaction.onCreate();
        LocalDateTime originalUpdatedAt = transaction.getUpdatedAt();

        // Wait a bit to ensure different timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        transaction.onUpdate();

        // Then
        assertNotNull(transaction.getUpdatedAt());
        assertTrue(transaction.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should handle all transaction statuses")
    void shouldHandleAllTransactionStatuses() {
        // Test PENDING
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        assertEquals(Transaction.TransactionStatus.PENDING, transaction.getStatus());

        // Test APPROVED
        transaction.setStatus(Transaction.TransactionStatus.APPROVED);
        assertEquals(Transaction.TransactionStatus.APPROVED, transaction.getStatus());

        // Test DECLINED
        transaction.setStatus(Transaction.TransactionStatus.DECLINED);
        assertEquals(Transaction.TransactionStatus.DECLINED, transaction.getStatus());

        // Test ERROR
        transaction.setStatus(Transaction.TransactionStatus.ERROR);
        assertEquals(Transaction.TransactionStatus.ERROR, transaction.getStatus());
    }

    @Test
    @DisplayName("Should store rejection reason for declined transactions")
    void shouldStoreRejectionReasonForDeclinedTransactions() {
        // Given
        String rejectionReason = "Insufficient funds";

        // When
        transaction.setStatus(Transaction.TransactionStatus.DECLINED);
        transaction.setRejectionReason(rejectionReason);

        // Then
        assertEquals(Transaction.TransactionStatus.DECLINED, transaction.getStatus());
        assertEquals(rejectionReason, transaction.getRejectionReason());
    }

    @Test
    @DisplayName("Should handle decimal amounts correctly")
    void shouldHandleDecimalAmountsCorrectly() {
        // Given
        BigDecimal amount = new BigDecimal("1234.56");

        // When
        transaction.setAmount(amount);

        // Then
        assertEquals(amount, transaction.getAmount());
        assertEquals(2, transaction.getAmount().scale());
    }

    @Test
    @DisplayName("Should store currency code")
    void shouldStoreCurrencyCode() {
        // Test various currencies
        transaction.setCurrency("USD");
        assertEquals("USD", transaction.getCurrency());

        transaction.setCurrency("EUR");
        assertEquals("EUR", transaction.getCurrency());

        transaction.setCurrency("GBP");
        assertEquals("GBP", transaction.getCurrency());
    }

    @Test
    @DisplayName("Should validate card expiry format")
    void shouldValidateCardExpiryFormat() {
        // Given
        String validExpiry = "12/2025";

        // When
        transaction.setCardExpiry(validExpiry);

        // Then
        assertEquals(validExpiry, transaction.getCardExpiry());
        assertTrue(transaction.getCardExpiry().matches("\\d{2}/\\d{4}"));
    }

    @Test
    @DisplayName("Should store processed timestamp")
    void shouldStoreProcessedTimestamp() {
        // Given
        LocalDateTime processedAt = LocalDateTime.now();

        // When
        transaction.setProcessedAt(processedAt);
        transaction.setStatus(Transaction.TransactionStatus.APPROVED);

        // Then
        assertNotNull(transaction.getProcessedAt());
        assertEquals(processedAt, transaction.getProcessedAt());
    }

    @Test
    @DisplayName("Should handle issuer response codes")
    void shouldHandleIssuerResponseCodes() {
        // Test approved response
        transaction.setResponseCode("00");
        transaction.setIssuerResponse("APPROVED");
        assertEquals("00", transaction.getResponseCode());
        assertEquals("APPROVED", transaction.getIssuerResponse());

        // Test declined response
        transaction.setResponseCode("51");
        transaction.setIssuerResponse("DECLINED");
        assertEquals("51", transaction.getResponseCode());
        assertEquals("DECLINED", transaction.getIssuerResponse());
    }

    @Test
    @DisplayName("Should support operation types")
    void shouldSupportOperationTypes() {
        // Test different operation types
        transaction.setOperationType("PURCHASE");
        assertEquals("PURCHASE", transaction.getOperationType());

        transaction.setOperationType("REFUND");
        assertEquals("REFUND", transaction.getOperationType());

        transaction.setOperationType("AUTHORIZATION");
        assertEquals("AUTHORIZATION", transaction.getOperationType());
    }

    @Test
    @DisplayName("Should support lombok equals and hashCode")
    void shouldSupportLombokEqualsAndHashCode() {
        // Given
        String txnId = "txn-123";
        Transaction transaction1 = Transaction.builder()
                .transactionId(txnId)
                .merchantId("MERCHANT_001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .cardToken("tok_123")
                .cardExpiry("12/2025")
                .operationType("PURCHASE")
                .status(Transaction.TransactionStatus.APPROVED)
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId(txnId)
                .merchantId("MERCHANT_001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .cardToken("tok_123")
                .cardExpiry("12/2025")
                .operationType("PURCHASE")
                .status(Transaction.TransactionStatus.APPROVED)
                .build();

        // Then
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    @DisplayName("Should support lombok toString")
    void shouldSupportLombokToString() {
        // Given
        transaction.setMerchantId("MERCHANT_001");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setStatus(Transaction.TransactionStatus.APPROVED);

        // When
        String toString = transaction.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("MERCHANT_001"));
        assertTrue(toString.contains("APPROVED"));
    }

    @Test
    @DisplayName("Should handle error status with rejection reason")
    void shouldHandleErrorStatusWithRejectionReason() {
        // Given
        String errorReason = "Connection timeout to issuer";

        // When
        transaction.setStatus(Transaction.TransactionStatus.ERROR);
        transaction.setRejectionReason(errorReason);

        // Then
        assertEquals(Transaction.TransactionStatus.ERROR, transaction.getStatus());
        assertEquals(errorReason, transaction.getRejectionReason());
    }

    @Test
    @DisplayName("Should allow null optional fields")
    void shouldAllowNullOptionalFields() {
        // Given & When
        transaction.setTransactionId("txn-123");
        transaction.setMerchantId("MERCHANT_001");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("USD");
        transaction.setCardToken("tok_123");
        transaction.setCardExpiry("12/2025");
        transaction.setOperationType("PURCHASE");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setResponseCode(null);
        transaction.setIssuerResponse(null);
        transaction.setRejectionReason(null);
        transaction.setProcessedAt(null);

        // Then
        assertNull(transaction.getResponseCode());
        assertNull(transaction.getIssuerResponse());
        assertNull(transaction.getRejectionReason());
        assertNull(transaction.getProcessedAt());
    }
}
