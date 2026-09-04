package com.ledgerlens.service;

import com.ledgerlens.entity.AppUser;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MerchantContext {
    private final AppUserRepository users;
    public MerchantContext(AppUserRepository users) { this.users = users; }
    public String merchantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return "merchant_a";
        return users.findByUsername(auth.getName())
                .map(user -> user.getMerchant() != null ? user.getMerchant().getMerchantId() : "merchant_a")
                .orElseGet(() -> auth.getName().startsWith("merchant_b") ? "merchant_b" : "merchant_a");
    }
}
