package com.acquirer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a payment transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment request from merchant")
public class PaymentRequest {

    @NotBlank(message = "Merchant ID is required")
    @Schema(description = "Unique merchant identifier", example = "MERCHANT_001")
    private String merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    private String currency;

    @NotBlank(message = "Card token is required")
    @Schema(description = "Hashed PAN or card token", example = "tok_4532015112830366")
    private String cardToken;

    @NotBlank(message = "Card expiry is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{4}$", message = "Card expiry must be in format MM/YYYY")
    @Schema(description = "Card expiration date", example = "12/2025")
    private String cardExpiry;

    @NotBlank(message = "Operation type is required")
    @Schema(description = "Type of operation", example = "PURCHASE", allowableValues = {"PURCHASE", "REFUND"})
    private String operationType;
}
