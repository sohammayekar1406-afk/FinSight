package com.ledgerlens.config;

import com.ledgerlens.service.ReconciliationLockService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ReconciliationLockInitializer {
    @Bean
    ApplicationRunner globalReconciliationLockRunner(ReconciliationLockService lockService) {
        return args -> lockService.ensureLockExists();
    }
}
