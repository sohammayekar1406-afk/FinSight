package com.ledgerlens.dto;

import java.math.BigDecimal;

public class SoftAnomalyDto {

    private boolean anomalyDetected;
    private String metric;
    private BigDecimal baseline;
    private BigDecimal currentValue;
    private BigDecimal deviation;
    private String explanation;

    public SoftAnomalyDto() {}

    public SoftAnomalyDto(boolean anomalyDetected, String metric, BigDecimal baseline, 
                         BigDecimal currentValue, BigDecimal deviation, String explanation) {
        this.anomalyDetected = anomalyDetected;
        this.metric = metric;
        this.baseline = baseline;
        this.currentValue = currentValue;
        this.deviation = deviation;
        this.explanation = explanation;
    }

    public static SoftAnomalyDtoBuilder builder() { return new SoftAnomalyDtoBuilder(); }

    public boolean isAnomalyDetected() { return anomalyDetected; }
    public void setAnomalyDetected(boolean anomalyDetected) { this.anomalyDetected = anomalyDetected; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public BigDecimal getBaseline() { return baseline; }
    public void setBaseline(BigDecimal baseline) { this.baseline = baseline; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public BigDecimal getDeviation() { return deviation; }
    public void setDeviation(BigDecimal deviation) { this.deviation = deviation; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public static class SoftAnomalyDtoBuilder {
        private boolean anomalyDetected;
        private String metric;
        private BigDecimal baseline;
        private BigDecimal currentValue;
        private BigDecimal deviation;
        private String explanation;

        public SoftAnomalyDtoBuilder anomalyDetected(boolean anomalyDetected) { 
            this.anomalyDetected = anomalyDetected; 
            return this; 
        }
        public SoftAnomalyDtoBuilder metric(String metric) { 
            this.metric = metric; 
            return this; 
        }
        public SoftAnomalyDtoBuilder baseline(BigDecimal baseline) { 
            this.baseline = baseline; 
            return this; 
        }
        public SoftAnomalyDtoBuilder currentValue(BigDecimal currentValue) { 
            this.currentValue = currentValue; 
            return this; 
        }
        public SoftAnomalyDtoBuilder deviation(BigDecimal deviation) { 
            this.deviation = deviation; 
            return this; 
        }
        public SoftAnomalyDtoBuilder explanation(String explanation) { 
            this.explanation = explanation; 
            return this; 
        }

        public SoftAnomalyDto build() {
            return new SoftAnomalyDto(anomalyDetected, metric, baseline, currentValue, deviation, explanation);
        }
    }
}
