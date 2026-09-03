package com.ledgerlens.security;

import com.ledgerlens.entity.AuditLog;
import com.ledgerlens.entity.AppUser;
import com.ledgerlens.repository.AppUserRepository;
import com.ledgerlens.repository.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Optional;

@Component
public class SecurityAuditEventListener {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public SecurityAuditEventListener(AuditLogRepository auditLogRepository, AppUserRepository appUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String merchantId = resolveMerchantId(username);
        String details = String.format("{\"username\":\"%s\",\"status\":\"AUTHENTICATION_SUCCESS\"}", username);

        AuditLog log = AuditLog.builder()
                .entityType("SECURITY")
                .entityId(UUID.nameUUIDFromBytes(username.getBytes()))
                .merchantId(merchantId)
                .action("SECURITY_AUTHENTICATION_SUCCESS")
                .performedBy(username)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String merchantId = resolveMerchantId(username);
        String exceptionMessage = event.getException() != null ? event.getException().getMessage() : "Bad credentials";
        String details = String.format("{\"username\":\"%s\",\"status\":\"AUTHENTICATION_FAILURE\",\"reason\":\"%s\"}", username, exceptionMessage.replace("\"", "\\\""));

        AuditLog log = AuditLog.builder()
                .entityType("SECURITY")
                .entityId(UUID.nameUUIDFromBytes((username != null ? username : "anonymous").getBytes()))
                .merchantId(merchantId)
                .action("SECURITY_AUTHENTICATION_FAILURE")
                .performedBy(username != null ? username : "ANONYMOUS")
                .details(details)
                .build();

        auditLogRepository.save(log);
    }

    private String resolveMerchantId(String username) {
        if (username == null || username.isBlank()) {
            return "UNKNOWN";
        }
        Optional<AppUser> user = appUserRepository.findByUsername(username);
        if (user.isPresent() && user.get().getMerchant() != null) {
            return user.get().getMerchant().getMerchantId();
        }
        return "UNKNOWN";
    }
}
