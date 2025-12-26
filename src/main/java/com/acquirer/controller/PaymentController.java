package com.acquirer.controller;

import com.acquirer.dto.PaymentRequest;
import com.acquirer.dto.PaymentResponse;
import com.acquirer.entity.Transaction.TransactionStatus;
import com.acquirer.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
        summary = "Process payment",
        description = "Submit a payment request for processing. The request will be validated, " +
                     "sent to the issuer for authorization, and a response will be returned."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Payment processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or business rule validation failed"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Merchant not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        
        log.info("Received payment request for merchant: {}", request.getMerchantId());
        PaymentResponse response = paymentService.processPayment(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transactionId}")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieve transaction details by transaction ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transaction found",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        )
    })
    public ResponseEntity<PaymentResponse> getTransaction(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable String transactionId) {
        
        log.debug("Retrieving transaction: {}", transactionId);
        PaymentResponse response = paymentService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Query transactions",
        description = "Get a list of transactions filtered by merchant ID and/or status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Transactions retrieved successfully"
        )
    })
    public ResponseEntity<List<PaymentResponse>> queryTransactions(
            @Parameter(description = "Merchant ID to filter by")
            @RequestParam(required = false) String merchantId,
            
            @Parameter(description = "Transaction status to filter by")
            @RequestParam(required = false) TransactionStatus status) {
        
        log.debug("Querying transactions - merchantId: {}, status: {}", merchantId, status);
        List<PaymentResponse> responses = paymentService.queryTransactions(merchantId, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the payment service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Acquirer Service is running");
    }
}
