package com.ledgerlens.dto;

public class RunInvestigationsResultDto {

    private int exceptionsProcessed;
    private int investigationsCreated;
    private int alreadyInvestigated;
    private int autoResolved;
    private int sentToHuman;

    public RunInvestigationsResultDto() {}

    public RunInvestigationsResultDto(int exceptionsProcessed, int investigationsCreated, int alreadyInvestigated, int autoResolved, int sentToHuman) {
        this.exceptionsProcessed = exceptionsProcessed;
        this.investigationsCreated = investigationsCreated;
        this.alreadyInvestigated = alreadyInvestigated;
        this.autoResolved = autoResolved;
        this.sentToHuman = sentToHuman;
    }

    public static RunInvestigationsResultDtoBuilder builder() { return new RunInvestigationsResultDtoBuilder(); }

    public int getExceptionsProcessed() { return exceptionsProcessed; }
    public void setExceptionsProcessed(int exceptionsProcessed) { this.exceptionsProcessed = exceptionsProcessed; }
    public int getInvestigationsCreated() { return investigationsCreated; }
    public void setInvestigationsCreated(int investigationsCreated) { this.investigationsCreated = investigationsCreated; }
    public int getAlreadyInvestigated() { return alreadyInvestigated; }
    public void setAlreadyInvestigated(int alreadyInvestigated) { this.alreadyInvestigated = alreadyInvestigated; }
    public int getAutoResolved() { return autoResolved; }
    public void setAutoResolved(int autoResolved) { this.autoResolved = autoResolved; }
    public int getSentToHuman() { return sentToHuman; }
    public void setSentToHuman(int sentToHuman) { this.sentToHuman = sentToHuman; }

    public static class RunInvestigationsResultDtoBuilder {
        private int exceptionsProcessed;
        private int investigationsCreated;
        private int alreadyInvestigated;
        private int autoResolved;
        private int sentToHuman;

        public RunInvestigationsResultDtoBuilder exceptionsProcessed(int exceptionsProcessed) { this.exceptionsProcessed = exceptionsProcessed; return this; }
        public RunInvestigationsResultDtoBuilder investigationsCreated(int investigationsCreated) { this.investigationsCreated = investigationsCreated; return this; }
        public RunInvestigationsResultDtoBuilder alreadyInvestigated(int alreadyInvestigated) { this.alreadyInvestigated = alreadyInvestigated; return this; }
        public RunInvestigationsResultDtoBuilder autoResolved(int autoResolved) { this.autoResolved = autoResolved; return this; }
        public RunInvestigationsResultDtoBuilder sentToHuman(int sentToHuman) { this.sentToHuman = sentToHuman; return this; }

        public RunInvestigationsResultDto build() {
            return new RunInvestigationsResultDto(exceptionsProcessed, investigationsCreated, alreadyInvestigated, autoResolved, sentToHuman);
        }
    }
}
