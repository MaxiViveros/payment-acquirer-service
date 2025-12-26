package com.acquirer.dto;

import com.acquirer.entity.Transaction.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment response to merchant")
public class PaymentResponse {

    @Schema(description = "Unique transaction identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String transactionId;

    @Schema(description = "Transaction status", example = "APPROVED")
    private TransactionStatus status;

    @Schema(description = "Response code from issuer", example = "00")
    private String responseCode;

    @Schema(description = "Response message", example = "Transaction approved")
    private String message;

    @Schema(description = "Transaction timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Merchant ID", example = "MERCHANT_001")
    private String merchantId;

    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "USD")
    private String currency;
}
