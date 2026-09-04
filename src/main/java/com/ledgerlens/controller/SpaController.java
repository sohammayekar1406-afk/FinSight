package com.ledgerlens.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA (Single Page Application) fallback controller.
 *
 * React Router manages routing entirely on the client side. When a user
 * directly navigates to (or refreshes) a deep link like /dashboard, /login,
 * /exceptions, etc., the browser makes a real GET request to Spring Boot.
 * Without this controller, Spring Boot would return 404 because it has no
 * explicit mapping for those paths.
 *
 * This controller catches every GET request that is NOT:
 *  - an /api/** endpoint  (handled by @RestController beans)
 *  - a static resource   (handled by Spring's ResourceHttpRequestHandler: /assets/*, *.js, *.css, etc.)
 *
 * …and forwards it to /index.html so React Router can take over on the client.
 */
@Controller
public class SpaController {

    /**
     * Catch-all for React Router paths.
     *
     * Spring evaluates more-specific mappings first, so /api/** handlers in
     * @RestController classes always win. Static resources (matched by
     * ResourceHttpRequestHandler via spring.mvc.static-path-pattern=/**) also
     * take priority because they are registered as resource handlers, not
     * controller handlers.
     *
     * The patterns below cover every non-API, non-static path used by the app:
     *   /               → redirect root to React app entry point
     *   /login          → LoginPage
     *   /dashboard      → DashboardPage
     *   /exceptions     → ExceptionsPage
     *   /exceptions/**  → individual exception detail pages
     *   /investigations → InvestigationsPage
     *   /audit-logs     → AuditLogsPage
     *   /settings       → SettingsPage
     *   /landing        → LandingPage (if accessed directly)
     *
     * "forward:" keeps the browser URL intact; the server returns index.html
     * content without issuing an HTTP redirect, so the URL stays as /dashboard.
     */
    @GetMapping({
        "/",
        "/login",
        "/dashboard",
        "/exceptions",
        "/exceptions/**",
        "/investigations",
        "/investigations/**",
        "/transactions",
        "/transactions/**",
        "/audit-logs",
        "/settings",
        "/landing",
        "/landing/**"
    })
    public String spa() {
        return "forward:/index.html";
    }
}
