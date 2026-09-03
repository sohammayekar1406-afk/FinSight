package com.ledgerlens.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "merchant_settings", uniqueConstraints = @UniqueConstraint(name = "uk_merchant_settings_merchant", columnNames = "merchant_id"))
public class MerchantSettings {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @OneToOne(optional = false) @JoinColumn(name = "merchant_id", nullable = false, unique = true) private Merchant merchant;
    @Column(name = "settlement_delay_hours", nullable = false) private int settlementDelayHours = 24;
    @Column(name = "fee_rate", precision = 8, scale = 4) private BigDecimal feeRate;
    @Column(name = "fee_tolerance", nullable = false, precision = 15, scale = 2) private BigDecimal feeTolerance = BigDecimal.ZERO;
    public MerchantSettings() {}
    public MerchantSettings(Merchant merchant, int delay, BigDecimal rate, BigDecimal tolerance) { this.merchant=merchant; settlementDelayHours=delay; feeRate=rate; feeTolerance=tolerance; }
    public int getSettlementDelayHours() { return settlementDelayHours; }
    public BigDecimal getFeeRate() { return feeRate; }
    public BigDecimal getFeeTolerance() { return feeTolerance; }
}
