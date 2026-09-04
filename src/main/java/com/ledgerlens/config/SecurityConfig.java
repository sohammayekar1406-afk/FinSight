package com.ledgerlens.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration.
 *
 * Since the React frontend is now compiled and served as static files from
 * Spring Boot itself (classpath:/static/), the frontend and API share the
 * same origin (http://localhost:8080).  Cross-origin requests no longer occur,
 * so the old CORS configuration has been removed.
 *
 * Static assets (JS, CSS, fonts, favicon) and the SPA entry-point (/index.html,
 * /login, /dashboard, …) are all permit-all.  Only /api/** endpoints carry
 * role-based access control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${SECURITY_OPERATOR_USERNAME:operator}")
    private String operatorUsername;

    @Value("${SECURITY_OPERATOR_PASSWORD:operator123}")
    private String operatorPassword;

    @Value("${SECURITY_ANALYST_USERNAME:analyst}")
    private String analystUsername;

    @Value("${SECURITY_ANALYST_PASSWORD:analyst123}")
    private String analystPassword;

    @Value("${SECURITY_ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${SECURITY_ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails operator = User.builder()
                .username(operatorUsername)
                .password(encoder.encode(operatorPassword))
                .roles("OPERATOR")
                .build();

        UserDetails analyst = User.builder()
                .username(analystUsername)
                .password(encoder.encode(analystPassword))
                .roles("OPERATOR", "ANALYST")
                .build();

        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(encoder.encode(adminPassword))
                .roles("OPERATOR", "ANALYST", "ADMIN")
                .build();

        UserDetails merchantBOperator = User.builder().username("merchant_b_operator").password(encoder.encode("operator123")).roles("OPERATOR").build();
        UserDetails merchantBAnalyst = User.builder().username("merchant_b_analyst").password(encoder.encode("analyst123")).roles("OPERATOR", "ANALYST").build();
        UserDetails merchantBAdmin = User.builder().username("merchant_b_admin").password(encoder.encode("admin123")).roles("OPERATOR", "ANALYST", "ADMIN").build();

        return new InMemoryUserDetailsManager(operator, analyst, admin, merchantBOperator, merchantBAnalyst, merchantBAdmin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS is not needed: frontend and API share the same origin (port 8080).
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // ── Public: static assets & SPA shell pages ────────────────────────
                .requestMatchers(
                    // Health probe
                    "/api/health",
                    // React SPA entry point and its forwarded routes
                    "/", "/index.html",
                    "/login", "/dashboard", "/exceptions", "/exceptions/**",
                    "/investigations", "/investigations/**",
                    "/transactions", "/transactions/**",
                    "/audit-logs", "/settings", "/landing", "/landing/**",
                    // Vite build output: hashed JS/CSS bundles live under /assets/
                    "/assets/**",
                    // Legacy static folder still used by old vanilla JS dashboard
                    "/css/**", "/js/**",
                    // Browser standard files
                    "/favicon.ico", "/favicon.svg", "/robots.txt", "/manifest.json",
                    // Swagger / OpenAPI
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html"
                ).permitAll()

                // ── Role-gated API endpoints ────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/exceptions/**", "/api/investigations/**", "/api/dashboard/**",
                    "/api/audit-logs/**", "/api/orders/**", "/api/payments/**",
                    "/api/settlements/**", "/api/refunds/**", "/api/fees/**",
                    "/api/adjustments/**"
                ).hasAnyRole("OPERATOR", "ANALYST", "ADMIN")

                .requestMatchers(HttpMethod.POST,
                    "/api/investigations/run", "/api/investigations/{exceptionId}",
                    "/api/reconciliation/**"
                ).hasAnyRole("ANALYST", "ADMIN")

                .requestMatchers(HttpMethod.POST,
                    "/api/investigations/{exceptionId}/resolve", "/api/demo/seed",
                    "/api/demo/validate", "/api/orders/**", "/api/payments/**",
                    "/api/settlements/**", "/api/refunds/**", "/api/fees/**",
                    "/api/adjustments/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.authenticationEntryPoint(
                // Return 401 JSON without WWW-Authenticate header so the browser
                // does not open its native Basic Auth dialog
                (request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" +
                        authException.getMessage() + "\"}"
                    );
                }
            ));

        return http.build();
    }
}
