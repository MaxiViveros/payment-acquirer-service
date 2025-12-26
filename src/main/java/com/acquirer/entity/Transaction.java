package com.acquirer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment transaction in the acquirer system.
 * Contains all transaction details, status, and audit information for traceability.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_merchant_status", columnList = "merchant_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private String transactionId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "card_token", nullable = false)
    private String cardToken;

    @Column(name = "card_expiry", nullable = false, length = 7)
    private String cardExpiry;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Column(name = "issuer_response", length = 20)
    private String issuerResponse;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionStatus {
        PENDING,
        APPROVED,
        DECLINED,
        ERROR
    }
}
