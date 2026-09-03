package com.ledgerlens.dto;

/**
 * Phase 3 Forensic Reasoning: Represents a specific request for additional evidence
 */
public class EvidenceRequestDto {

    private String evidenceType;
    private String description;
    private String reason;
    private String expectedImpact;

    public EvidenceRequestDto() {}

    public EvidenceRequestDto(String evidenceType, String description, String reason, String expectedImpact) {
        this.evidenceType = evidenceType;
        this.description = description;
        this.reason = reason;
        this.expectedImpact = expectedImpact;
    }

    public static EvidenceRequestDtoBuilder builder() { return new EvidenceRequestDtoBuilder(); }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getExpectedImpact() { return expectedImpact; }
    public void setExpectedImpact(String expectedImpact) { this.expectedImpact = expectedImpact; }

    public static class EvidenceRequestDtoBuilder {
        private String evidenceType;
        private String description;
        private String reason;
        private String expectedImpact;

        public EvidenceRequestDtoBuilder evidenceType(String evidenceType) { 
            this.evidenceType = evidenceType; 
            return this; 
        }
        public EvidenceRequestDtoBuilder description(String description) { 
            this.description = description; 
            return this; 
        }
        public EvidenceRequestDtoBuilder reason(String reason) { 
            this.reason = reason; 
            return this; 
        }
        public EvidenceRequestDtoBuilder expectedImpact(String expectedImpact) { 
            this.expectedImpact = expectedImpact; 
            return this; 
        }

        public EvidenceRequestDto build() {
            return new EvidenceRequestDto(evidenceType, description, reason, expectedImpact);
        }
    }
}
