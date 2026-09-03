package com.ledgerlens.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LedgerLens Financial Reconciliation & Exception Investigation API")
                        .version("1.0.0")
                        .description("Production-grade financial reconciliation engine, exception detection, AI investigation layer with Gemini + rule fallback, audit logging, and financial operations dashboard API.")
                        .contact(new Contact()
                                .name("LedgerLens Engineering Team")
                                .email("engineering@ledgerlens.internal")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .name("basicAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("HTTP Basic authentication supporting OPERATOR, ANALYST, and ADMIN roles.")
                        )
                );
    }
}
