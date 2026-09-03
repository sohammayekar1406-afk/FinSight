package com.ledgerlens.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchants", uniqueConstraints = @UniqueConstraint(name = "uk_merchants_merchant_id", columnNames = "merchant_id"))
public class Merchant {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "merchant_id", nullable = false, unique = true, length = 64) private String merchantId;
    @Column(nullable = false, length = 128) private String name;
    @Column(nullable = false) private boolean active = true;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    public Merchant() {}
    public Merchant(String merchantId, String name) { this.merchantId = merchantId; this.name = name; }
    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
