package com.ledgerlens.dto;

/**
 * Phase 3 Forensic Reasoning: Represents a detected contradiction in evidence
 */
public class ContradictionDto {

    private String contradiction;
    private String evidenceA;
    private String evidenceB;
    private ContradictionSeverity severity;
    private String resolution;
    private boolean unresolved;

    public enum ContradictionSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public ContradictionDto() {}

    public ContradictionDto(String contradiction, String evidenceA, String evidenceB, 
                           ContradictionSeverity severity, String resolution, boolean unresolved) {
        this.contradiction = contradiction;
        this.evidenceA = evidenceA;
        this.evidenceB = evidenceB;
        this.severity = severity;
        this.resolution = resolution;
        this.unresolved = unresolved;
    }

    public static ContradictionDtoBuilder builder() { return new ContradictionDtoBuilder(); }

    public String getContradiction() { return contradiction; }
    public void setContradiction(String contradiction) { this.contradiction = contradiction; }

    public String getEvidenceA() { return evidenceA; }
    public void setEvidenceA(String evidenceA) { this.evidenceA = evidenceA; }

    public String getEvidenceB() { return evidenceB; }
    public void setEvidenceB(String evidenceB) { this.evidenceB = evidenceB; }

    public ContradictionSeverity getSeverity() { return severity; }
    public void setSeverity(ContradictionSeverity severity) { this.severity = severity; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public boolean isUnresolved() { return unresolved; }
    public void setUnresolved(boolean unresolved) { this.unresolved = unresolved; }

    public static class ContradictionDtoBuilder {
        private String contradiction;
        private String evidenceA;
        private String evidenceB;
        private ContradictionSeverity severity;
        private String resolution;
        private boolean unresolved;

        public ContradictionDtoBuilder contradiction(String contradiction) { 
            this.contradiction = contradiction; 
            return this; 
        }
        public ContradictionDtoBuilder evidenceA(String evidenceA) { 
            this.evidenceA = evidenceA; 
            return this; 
        }
        public ContradictionDtoBuilder evidenceB(String evidenceB) { 
            this.evidenceB = evidenceB; 
            return this; 
        }
        public ContradictionDtoBuilder severity(ContradictionSeverity severity) { 
            this.severity = severity; 
            return this; 
        }
        public ContradictionDtoBuilder resolution(String resolution) { 
            this.resolution = resolution; 
            return this; 
        }
        public ContradictionDtoBuilder unresolved(boolean unresolved) { 
            this.unresolved = unresolved; 
            return this; 
        }

        public ContradictionDto build() {
            return new ContradictionDto(contradiction, evidenceA, evidenceB, 
                    severity, resolution, unresolved);
        }
    }
}
