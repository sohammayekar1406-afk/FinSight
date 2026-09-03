package com.ledgerlens.dto;

import java.util.List;

public class SeedResponseDto {
    private String message;
    private List<String> ordersCreated;
    private List<String> paymentsCreated;
    private List<String> refundsCreated;
    private List<String> feesCreated;
    private List<String> adjustmentsCreated;
    private List<String> settlementsCreated;

    public SeedResponseDto() {}

    public SeedResponseDto(String message, List<String> ordersCreated, List<String> paymentsCreated, List<String> refundsCreated, List<String> feesCreated, List<String> adjustmentsCreated, List<String> settlementsCreated) {
        this.message = message;
        this.ordersCreated = ordersCreated;
        this.paymentsCreated = paymentsCreated;
        this.refundsCreated = refundsCreated;
        this.feesCreated = feesCreated;
        this.adjustmentsCreated = adjustmentsCreated;
        this.settlementsCreated = settlementsCreated;
    }

    public static SeedResponseDtoBuilder builder() { return new SeedResponseDtoBuilder(); }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<String> getOrdersCreated() { return ordersCreated; }
    public void setOrdersCreated(List<String> ordersCreated) { this.ordersCreated = ordersCreated; }
    public List<String> getPaymentsCreated() { return paymentsCreated; }
    public void setPaymentsCreated(List<String> paymentsCreated) { this.paymentsCreated = paymentsCreated; }
    public List<String> getRefundsCreated() { return refundsCreated; }
    public void setRefundsCreated(List<String> refundsCreated) { this.refundsCreated = refundsCreated; }
    public List<String> getFeesCreated() { return feesCreated; }
    public void setFeesCreated(List<String> feesCreated) { this.feesCreated = feesCreated; }
    public List<String> getAdjustmentsCreated() { return adjustmentsCreated; }
    public void setAdjustmentsCreated(List<String> adjustmentsCreated) { this.adjustmentsCreated = adjustmentsCreated; }
    public List<String> getSettlementsCreated() { return settlementsCreated; }
    public void setSettlementsCreated(List<String> settlementsCreated) { this.settlementsCreated = settlementsCreated; }

    public static class SeedResponseDtoBuilder {
        private String message;
        private List<String> ordersCreated;
        private List<String> paymentsCreated;
        private List<String> refundsCreated;
        private List<String> feesCreated;
        private List<String> adjustmentsCreated;
        private List<String> settlementsCreated;

        public SeedResponseDtoBuilder message(String message) { this.message = message; return this; }
        public SeedResponseDtoBuilder ordersCreated(List<String> ordersCreated) { this.ordersCreated = ordersCreated; return this; }
        public SeedResponseDtoBuilder paymentsCreated(List<String> paymentsCreated) { this.paymentsCreated = paymentsCreated; return this; }
        public SeedResponseDtoBuilder refundsCreated(List<String> refundsCreated) { this.refundsCreated = refundsCreated; return this; }
        public SeedResponseDtoBuilder feesCreated(List<String> feesCreated) { this.feesCreated = feesCreated; return this; }
        public SeedResponseDtoBuilder adjustmentsCreated(List<String> adjustmentsCreated) { this.adjustmentsCreated = adjustmentsCreated; return this; }
        public SeedResponseDtoBuilder settlementsCreated(List<String> settlementsCreated) { this.settlementsCreated = settlementsCreated; return this; }

        public SeedResponseDto build() {
            return new SeedResponseDto(message, ordersCreated, paymentsCreated, refundsCreated, feesCreated, adjustmentsCreated, settlementsCreated);
        }
    }
}
