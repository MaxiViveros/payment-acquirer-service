package com.acquirer.repository;

import com.acquirer.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Merchant entity
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {

    /**
     * Find active merchants
     */
    List<Merchant> findByActiveTrue();

    /**
     * Find merchant by ID only if active
     */
    Optional<Merchant> findByMerchantIdAndActiveTrue(String merchantId);
}
