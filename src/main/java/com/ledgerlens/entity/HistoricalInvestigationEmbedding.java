package com.ledgerlens.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "historical_investigation_embeddings", indexes = {
    @Index(name = "idx_hist_embeddings_merchant", columnList = "merchant_id"),
    @Index(name = "idx_hist_embeddings_investigation", columnList = "investigation_id")
})
public class HistoricalInvestigationEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private Investigation investigation;

    @Column(name = "merchant_id", length = 64, nullable = false)
    private String merchantId;

    @Column(name = "source_text", columnDefinition = "TEXT", nullable = false)
    private String sourceText;

    @Convert(converter = VectorConverter.class)
    @Column(name = "embedding", columnDefinition = "TEXT")
    private List<Float> embedding;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public HistoricalInvestigationEmbedding() {}

    public HistoricalInvestigationEmbedding(UUID id, Investigation investigation, String merchantId, String sourceText, List<Float> embedding, OffsetDateTime createdAt) {
        this.id = id;
        this.investigation = investigation;
        this.merchantId = merchantId;
        this.sourceText = sourceText;
        this.embedding = embedding;
        this.createdAt = createdAt;
    }

    public static HistoricalInvestigationEmbeddingBuilder builder() {
        return new HistoricalInvestigationEmbeddingBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Investigation getInvestigation() { return investigation; }
    public void setInvestigation(Investigation investigation) { this.investigation = investigation; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getSourceText() { return sourceText; }
    public void setSourceText(String sourceText) { this.sourceText = sourceText; }
    public List<Float> getEmbedding() { return embedding; }
    public void setEmbedding(List<Float> embedding) { this.embedding = embedding; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class HistoricalInvestigationEmbeddingBuilder {
        private UUID id;
        private Investigation investigation;
        private String merchantId;
        private String sourceText;
        private List<Float> embedding;
        private OffsetDateTime createdAt;

        public HistoricalInvestigationEmbeddingBuilder id(UUID id) { this.id = id; return this; }
        public HistoricalInvestigationEmbeddingBuilder investigation(Investigation investigation) { this.investigation = investigation; return this; }
        public HistoricalInvestigationEmbeddingBuilder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public HistoricalInvestigationEmbeddingBuilder sourceText(String sourceText) { this.sourceText = sourceText; return this; }
        public HistoricalInvestigationEmbeddingBuilder embedding(List<Float> embedding) { this.embedding = embedding; return this; }
        public HistoricalInvestigationEmbeddingBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public HistoricalInvestigationEmbedding build() {
            return new HistoricalInvestigationEmbedding(id, investigation, merchantId, sourceText, embedding, createdAt);
        }
    }
}
