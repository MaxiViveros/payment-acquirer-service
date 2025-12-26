package com.acquirer.controller;

import com.acquirer.dto.PaymentRequest;
import com.acquirer.dto.PaymentResponse;
import com.acquirer.entity.Transaction.TransactionStatus;
import com.acquirer.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private PaymentResponse mockResponse;

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

        mockResponse = PaymentResponse.builder()
                .transactionId("test-transaction-id")
                .status(TransactionStatus.APPROVED)
                .responseCode("00")
                .message("Transaction approved")
                .timestamp(LocalDateTime.now())
                .merchantId("MERCHANT_001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("test-transaction-id"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.merchantId").value("MERCHANT_001"));
    }

    @Test
    void testProcessPayment_InvalidRequest() throws Exception {
        PaymentRequest invalidRequest = PaymentRequest.builder()
                .merchantId("")
                .build();

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTransaction_Success() throws Exception {
        when(paymentService.getTransaction("test-id")).thenReturn(mockResponse);

        mockMvc.perform(get("/payments/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("test-transaction-id"));
    }

    @Test
    void testQueryTransactions_Success() throws Exception {
        List<PaymentResponse> responses = Arrays.asList(mockResponse);
        when(paymentService.queryTransactions("MERCHANT_001", TransactionStatus.APPROVED))
                .thenReturn(responses);

        mockMvc.perform(get("/payments")
                .param("merchantId", "MERCHANT_001")
                .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("test-transaction-id"));
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/payments/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment Acquirer Service is running"));
    }
}
