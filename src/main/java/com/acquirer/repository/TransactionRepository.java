package com.acquirer.repository;

import com.acquirer.entity.Transaction;
import com.acquirer.entity.Transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find transactions by merchant ID
     */
    List<Transaction> findByMerchantId(String merchantId);

    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Find transactions by merchant ID and status
     */
    List<Transaction> findByMerchantIdAndStatus(String merchantId, TransactionStatus status);

    /**
     * Find transactions by merchant ID ordered by creation date (newest first)
     */
    List<Transaction> findByMerchantIdOrderByCreatedAtDesc(String merchantId);

    /**
     * Count transactions by merchant ID and status
     */
    long countByMerchantIdAndStatus(String merchantId, TransactionStatus status);

    /**
     * Find recent transactions for fraud detection
     */
    @Query("SELECT t FROM Transaction t WHERE t.merchantId = :merchantId " +
           "AND t.createdAt >= CURRENT_TIMESTAMP - 1 HOUR ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByMerchant(@Param("merchantId") String merchantId);
}
