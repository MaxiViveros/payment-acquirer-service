package com.acquirer.service;

import com.acquirer.dto.PaymentRequest;
import com.acquirer.dto.PaymentResponse;
import com.acquirer.entity.Merchant;
import com.acquirer.entity.Transaction;
import com.acquirer.entity.Transaction.TransactionStatus;
import com.acquirer.exception.MerchantNotFoundException;
import com.acquirer.exception.ValidationException;
import com.acquirer.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MerchantService merchantService;

    @Mock
    private ValidationService validationService;

    @Mock
    private IssuerService issuerService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private Merchant testMerchant;

    @BeforeEach
    void setUp() {
        validRequest = PaymentRequest.builder()
                .merchantId("MERCHANT_001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .cardToken("tok_1234567890123456")
                .cardExpiry("12/2025")
                .operationType("PURCHASE")
                .build();

        testMerchant = Merchant.builder()
                .merchantId("MERCHANT_001")
                .merchantName("Test Merchant")
                .maxTransactionAmount(new BigDecimal("5000.00"))
                .active(true)
                .build();
    }

    @Test
    void testProcessPayment_Success() {
        when(merchantService.isMerchantActive(anyString())).thenReturn(true);
        when(merchantService.getMerchantById(anyString())).thenReturn(testMerchant);
        when(validationService.validateAmount(any(), any()))
                .thenReturn(ValidationService.ValidationResult.valid());
        when(validationService.validateCardToken(anyString()))
                .thenReturn(ValidationService.ValidationResult.valid());
        when(validationService.validateCurrency(anyString()))
                .thenReturn(ValidationService.ValidationResult.valid());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(validRequest);

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals("MERCHANT_001", response.getMerchantId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testProcessPayment_MerchantNotFound() {
        when(merchantService.isMerchantActive(anyString())).thenReturn(false);

        assertThrows(MerchantNotFoundException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }

    @Test
    void testProcessPayment_ValidationFails() {
        when(merchantService.isMerchantActive(anyString())).thenReturn(true);
        when(merchantService.getMerchantById(anyString())).thenReturn(testMerchant);
        when(validationService.validateAmount(any(), any()))
                .thenReturn(ValidationService.ValidationResult.invalid("Amount too high"));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ValidationException.class, () -> {
            paymentService.processPayment(validRequest);
        });
    }

    @Test
    void testGetTransaction_Success() {
        Transaction transaction = Transaction.builder()
                .transactionId("test-id")
                .merchantId("MERCHANT_001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(TransactionStatus.APPROVED)
                .responseCode("00")
                .build();
        
        when(transactionRepository.findById("test-id")).thenReturn(Optional.of(transaction));

        PaymentResponse response = paymentService.getTransaction("test-id");

        assertNotNull(response);
        assertEquals("test-id", response.getTransactionId());
        assertEquals(TransactionStatus.APPROVED, response.getStatus());
    }

    @Test
    void testGetTransaction_NotFound() {
        when(transactionRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            paymentService.getTransaction("invalid-id");
        });
    }
}
