package com.ledgerlens.dto;

import com.ledgerlens.entity.enums.ExceptionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Phase 3 Forensic Reasoning: Represents a related exception for cross-exception correlation
 */
public class RelatedExceptionDto {

    public enum RelationshipType {
        SAME_PAYMENT,
        SAME_SETTLEMENT,
        SAME_REFUND,
        SAME_EXCEPTION_TYPE
    }

    private Long exceptionId;  // Internal database ID
    private ExceptionType exceptionType;
    private String merchantId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private RelationshipType relationshipType;
    private String relationshipReason;

    public RelatedExceptionDto() {}

    public RelatedExceptionDto(Long exceptionId, ExceptionType exceptionType, String merchantId, 
                              BigDecimal amount, LocalDateTime createdAt, 
                              RelationshipType relationshipType, String relationshipReason) {
        this.exceptionId = exceptionId;
        this.exceptionType = exceptionType;
        this.merchantId = merchantId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.relationshipType = relationshipType;
        this.relationshipReason = relationshipReason;
    }

    public static RelatedExceptionDtoBuilder builder() { return new RelatedExceptionDtoBuilder(); }

    public Long getExceptionId() { return exceptionId; }
    public void setExceptionId(Long exceptionId) { this.exceptionId = exceptionId; }

    public ExceptionType getExceptionType() { return exceptionType; }
    public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }

    public String getRelationshipReason() { return relationshipReason; }
    public void setRelationshipReason(String relationshipReason) { this.relationshipReason = relationshipReason; }

    public static class RelatedExceptionDtoBuilder {
        private Long exceptionId;
        private ExceptionType exceptionType;
        private String merchantId;
        private BigDecimal amount;
        private LocalDateTime createdAt;
        private RelationshipType relationshipType;
        private String relationshipReason;

        public RelatedExceptionDtoBuilder exceptionId(Long exceptionId) { 
            this.exceptionId = exceptionId; 
            return this; 
        }
        public RelatedExceptionDtoBuilder exceptionType(ExceptionType exceptionType) { 
            this.exceptionType = exceptionType; 
            return this; 
        }
        public RelatedExceptionDtoBuilder merchantId(String merchantId) { 
            this.merchantId = merchantId; 
            return this; 
        }
        public RelatedExceptionDtoBuilder amount(BigDecimal amount) { 
            this.amount = amount; 
            return this; 
        }
        public RelatedExceptionDtoBuilder createdAt(LocalDateTime createdAt) { 
            this.createdAt = createdAt; 
            return this; 
        }
        public RelatedExceptionDtoBuilder relationshipType(RelationshipType relationshipType) { 
            this.relationshipType = relationshipType; 
            return this; 
        }
        public RelatedExceptionDtoBuilder relationshipReason(String relationshipReason) { 
            this.relationshipReason = relationshipReason; 
            return this; 
        }

        public RelatedExceptionDto build() {
            return new RelatedExceptionDto(exceptionId, exceptionType, merchantId, amount, createdAt, relationshipType, relationshipReason);
        }
    }
}
