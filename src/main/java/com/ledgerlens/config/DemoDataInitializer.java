package com.ledgerlens.config;

import com.ledgerlens.service.SeedDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * Demo data initializer — active ONLY in the "test" (H2 demo/dev) profile.
 *
 * On a fresh H2 runtime the MerchantContext requires at least one persisted
 * AppUser → Merchant mapping before any merchant-scoped API call can succeed.
 * This runner calls SeedDataService.seedDemoData() idempotently at startup,
 * ensuring the mapping is always available without requiring an explicit
 * POST /api/demo/seed call first.
 *
 * Production profile: this bean is NOT loaded — no production side effects.
 * Security: does not bypass MerchantContext or weaken any authorization check.
 */
@Configuration
@Profile("test")
public class DemoDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    @Bean
    @Order(10)
    ApplicationRunner demoDataRunner(SeedDataService seedDataService) {
        return args -> {
            try {
                seedDataService.seedDemoData();
                log.info("DemoDataInitializer: demo merchant/user mappings seeded successfully (test profile).");
            } catch (Exception e) {
                // Non-fatal: tests seed their own data in @BeforeEach — this is a convenience only.
                log.warn("DemoDataInitializer non-fatal: {}", e.getMessage());
            }
        };
    }
}
