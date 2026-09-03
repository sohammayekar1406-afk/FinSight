package com.ledgerlens.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "finsight.ai")
public class AiProperties {

    private boolean enabled = false;
    private String provider = "gemini";
    private String apiKey = "";
    private String model = "gemini-1.5-flash";
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private int timeoutMs = 5000;

    // Phase 6: RAG Properties
    private String embeddingModel = "text-embedding-004";
    private double ragSimilarityThreshold = 0.50;
    private int ragMaxResults = 3;
    private boolean ragEnabled = true;

    public AiProperties() {}

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }

    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

    public double getRagSimilarityThreshold() { return ragSimilarityThreshold; }
    public void setRagSimilarityThreshold(double ragSimilarityThreshold) { this.ragSimilarityThreshold = ragSimilarityThreshold; }

    public int getRagMaxResults() { return ragMaxResults; }
    public void setRagMaxResults(int ragMaxResults) { this.ragMaxResults = ragMaxResults; }

    public boolean isRagEnabled() { return ragEnabled; }
    public void setRagEnabled(boolean ragEnabled) { this.ragEnabled = ragEnabled; }
}
