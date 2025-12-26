package com.acquirer.service;

import com.acquirer.entity.Merchant;
import com.acquirer.exception.MerchantNotFoundException;
import com.acquirer.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {
    private final MerchantRepository merchantRepository;

    @Transactional(readOnly = true)
    public Merchant getMerchantById(String merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found: " + merchantId));
    }

    @Transactional(readOnly = true)
    public boolean isMerchantActive(String merchantId) {
        return merchantRepository.findByMerchantIdAndActiveTrue(merchantId).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Merchant> getActiveMerchants() {
        return merchantRepository.findByActiveTrue();
    }

    @Transactional
    public Merchant saveMerchant(Merchant merchant) {
        log.info("Saving merchant: {}", merchant.getMerchantId());
        return merchantRepository.save(merchant);
    }

    @Transactional
    public void initializeDefaultMerchants() {
        if (merchantRepository.count() == 0) {
            log.info("Initializing default merchants...");
            
            merchantRepository.save(Merchant.builder()
                    .merchantId("MERCHANT_001")
                    .merchantName("Test Store Alpha")
                    .maxTransactionAmount(new BigDecimal("5000.00"))
                    .active(true)
                    .build());

            merchantRepository.save(Merchant.builder()
                    .merchantId("MERCHANT_002")
                    .merchantName("Test Store Beta")
                    .maxTransactionAmount(new BigDecimal("10000.00"))
                    .active(true)
                    .build());

            merchantRepository.save(Merchant.builder()
                    .merchantId("MERCHANT_003")
                    .merchantName("Test Store Gamma")
                    .maxTransactionAmount(new BigDecimal("1000.00"))
                    .active(true)
                    .build());

            log.info("Default merchants initialized");
        }
    }
}
