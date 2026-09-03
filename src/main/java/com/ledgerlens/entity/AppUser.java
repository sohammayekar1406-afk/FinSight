package com.ledgerlens.entity;

import jakarta.persistence.*;
import java.util.UUID;

/** Durable user-to-merchant association; authentication remains the existing Basic Auth setup. */
@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name = "uk_app_users_username", columnNames = "username"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, unique = true, length = 64) private String username;
    @ManyToOne(optional = false) @JoinColumn(name = "merchant_id", nullable = false) private Merchant merchant;
    @Column(nullable = false, length = 32) private String role;
    public AppUser() {}
    public AppUser(String username, Merchant merchant, String role) { this.username=username; this.merchant=merchant; this.role=role; }
    public String getUsername() { return username; }
    public Merchant getMerchant() { return merchant; }
}
