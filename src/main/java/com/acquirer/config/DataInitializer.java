package com.acquirer.config;

import com.acquirer.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize data on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final MerchantService merchantService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing application data...");
        merchantService.initializeDefaultMerchants();
        log.info("Application data initialized successfully");
    }
}
