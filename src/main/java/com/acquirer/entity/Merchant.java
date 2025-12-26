package com.acquirer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a merchant in the system.
 * Contains merchant configuration and business rules.
 */
@Entity
@Table(name = "merchants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @Column(name = "merchant_id", updatable = false, nullable = false)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "max_transaction_amount", precision = 19, scale = 2)
    private BigDecimal maxTransactionAmount;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
