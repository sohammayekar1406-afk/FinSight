package com.ledgerlens.service.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.config.AiProperties;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.HistoricalInvestigationEmbedding;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.repository.HistoricalInvestigationEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Phase 6.2: Historical Investigation Embedding Service
 * 
 * Embeds resolved historical investigations into pgvector format using Gemini text-embedding-004.
 * Hooked into investigation resolution flow. Non-blocking error handling on API failures.
 */
@Service
public class HistoricalInvestigationEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalInvestigationEmbeddingService.class);
    private static final int EMBEDDING_DIMENSIONS = 768;

    private final HistoricalInvestigationEmbeddingRepository embeddingRepository;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private RestClient restClient;

    public HistoricalInvestigationEmbeddingService(
            HistoricalInvestigationEmbeddingRepository embeddingRepository,
            AiProperties aiProperties,
            ObjectMapper objectMapper) {
        this.embeddingRepository = embeddingRepository;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    private RestClient getRestClient() {
        if (this.restClient == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            int timeout = aiProperties.getTimeoutMs() > 0 ? aiProperties.getTimeoutMs() : 5000;
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            this.restClient = RestClient.builder().requestFactory(factory).build();
        }
        return this.restClient;
    }

    /**
     * Builds structured text representation of a resolved investigation for embedding.
     * Contains only forensic financial context without raw PII.
     */
    public String buildEmbeddingText(Investigation investigation) {
        if (investigation == null || investigation.getException() == null) {
            return "";
        }

        FinancialException ex = investigation.getException();
        StringBuilder sb = new StringBuilder();

        sb.append("EXCEPTION_TYPE: ").append(ex.getExceptionType() != null ? ex.getExceptionType().name() : "UNKNOWN").append("\n");
        sb.append("SEVERITY: ").append(ex.getSeverity() != null ? ex.getSeverity().name() : "UNKNOWN").append("\n");

        if (ex.getDiscrepancyAmount() != null) {
            sb.append("DISCREPANCY_AMOUNT: ").append(ex.getDiscrepancyAmount().toPlainString()).append("\n");
        }

        if (ex.getDescription() != null && !ex.getDescription().isBlank()) {
            sb.append("DESCRIPTION: ").append(ex.getDescription().trim()).append("\n");
        }

        if (investigation.getLikelyRootCause() != null && !investigation.getLikelyRootCause().isBlank()) {
            sb.append("ROOT_CAUSE: ").append(investigation.getLikelyRootCause().trim()).append("\n");
        }

        if (investigation.getRecommendedAction() != null) {
            sb.append("RESOLUTION_ACTION: ").append(investigation.getRecommendedAction().name()).append("\n");
        }

        if (investigation.getSummary() != null && !investigation.getSummary().isBlank()) {
            sb.append("INVESTIGATION_FINDINGS: ").append(investigation.getSummary().trim()).append("\n");
        }

        if (investigation.getSupportingEvidence() != null && !investigation.getSupportingEvidence().isBlank()) {
            sb.append("FINANCIAL_EVIDENCE_SUMMARY: ").append(investigation.getSupportingEvidence().trim()).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Builds structured query text from current exception and metadata for semantic retrieval.
     */
    public String buildQueryText(FinancialException exception, String evidenceSummary) {
        if (exception == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("EXCEPTION_TYPE: ").append(exception.getExceptionType() != null ? exception.getExceptionType().name() : "UNKNOWN").append("\n");
        sb.append("SEVERITY: ").append(exception.getSeverity() != null ? exception.getSeverity().name() : "UNKNOWN").append("\n");

        if (exception.getDiscrepancyAmount() != null) {
            sb.append("DISCREPANCY_AMOUNT: ").append(exception.getDiscrepancyAmount().toPlainString()).append("\n");
        }

        if (exception.getDescription() != null && !exception.getDescription().isBlank()) {
            sb.append("DESCRIPTION: ").append(exception.getDescription().trim()).append("\n");
        }

        if (evidenceSummary != null && !evidenceSummary.isBlank()) {
            sb.append("INVESTIGATION_FINDINGS: ").append(evidenceSummary.trim()).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Generates embedding vector for a given text using Gemini API or deterministic fallback.
     */
    public List<Float> generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // Real API call if API key configured
        if (aiProperties.isEnabled() && aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            try {
                String model = aiProperties.getEmbeddingModel() != null ? aiProperties.getEmbeddingModel() : "text-embedding-004";
                String url = String.format("%s/v1beta/models/%s:embedContent?key=%s",
                        aiProperties.getBaseUrl(),
                        model,
                        aiProperties.getApiKey());

                String requestBody = objectMapper.writeValueAsString(Map.of(
                        "content", Map.of(
                                "parts", List.of(Map.of("text", text))
                        )
                ));

                String rawResponse = getRestClient().post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(rawResponse);
                JsonNode valuesNode = root.path("embedding").path("values");

                if (valuesNode.isArray() && !valuesNode.isEmpty()) {
                    List<Float> vector = new ArrayList<>();
                    for (JsonNode val : valuesNode) {
                        vector.add((float) val.asDouble());
                    }
                    return vector;
                }
            } catch (Exception e) {
                log.warn("Gemini embedding API call failed: {}. Falling back to deterministic pseudo-embedding.", e.getMessage());
            }
        }

        // Deterministic pseudo-embedding for testing / offline mode
        return generateDeterministicVector(text, EMBEDDING_DIMENSIONS);
    }

    /**
     * Embeds and persists a resolved investigation.
     * Guaranteed non-blocking on failure.
     */
    @Transactional
    public Optional<HistoricalInvestigationEmbedding> embedAndPersistResolvedInvestigation(Investigation investigation) {
        if (investigation == null || investigation.getException() == null) {
            return Optional.empty();
        }

        FinancialException ex = investigation.getException();
        ExceptionStatus status = ex.getStatus();
        boolean isResolved = (status == ExceptionStatus.RESOLVED_AUTO || status == ExceptionStatus.RESOLVED_MANUAL);

        if (!isResolved) {
            log.debug("Skipping embedding for unresolved investigation {}", investigation.getId());
            return Optional.empty();
        }

        try {
            String sourceText = buildEmbeddingText(investigation);
            if (sourceText.isBlank()) {
                return Optional.empty();
            }

            List<Float> embeddingVector = generateEmbedding(sourceText);

            // Upsert embedding entity
            Optional<HistoricalInvestigationEmbedding> existingOpt = 
                    embeddingRepository.findByInvestigation_Id(investigation.getId());

            HistoricalInvestigationEmbedding embeddingEntity = existingOpt.orElseGet(() -> 
                    HistoricalInvestigationEmbedding.builder()
                            .investigation(investigation)
                            .merchantId(ex.getMerchantId())
                            .build()
            );

            embeddingEntity.setSourceText(sourceText);
            embeddingEntity.setEmbedding(embeddingVector);
            embeddingEntity.setMerchantId(ex.getMerchantId());

            HistoricalInvestigationEmbedding saved = embeddingRepository.save(embeddingEntity);
            log.info("Successfully generated and stored embedding for investigation {} (merchant: {})",
                    investigation.getId(), ex.getMerchantId());
            return Optional.of(saved);

        } catch (Exception e) {
            // Failure mode requirement: log and do not block resolution
            log.error("Failed to generate or persist embedding for investigation {}: {}",
                    investigation.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Generates a deterministic normalized N-dimensional vector from input text using feature hashing (unigram + bigram).
     * Produces consistent, semantically correlatable vector spaces for testing and offline evaluation.
     */
    public List<Float> generateDeterministicVector(String text, int dimensions) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        float[] vec = new float[dimensions];
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9_\\s\\.]", " ");
        String[] tokens = normalized.split("\\s+");

        if (tokens.length == 0) {
            return List.of();
        }

        // Unigrams
        for (String token : tokens) {
            if (token.isBlank()) continue;
            int hash = token.hashCode();
            int bucket = Math.abs(hash) % dimensions;
            float sign = ((hash & 1) == 0) ? 1.0f : -1.0f;
            vec[bucket] += sign * 1.0f;
        }

        // Bigrams for phrase capture
        for (int i = 0; i < tokens.length - 1; i++) {
            if (tokens[i].isBlank() || tokens[i + 1].isBlank()) continue;
            String bigram = tokens[i] + "_" + tokens[i + 1];
            int hash = bigram.hashCode();
            int bucket = Math.abs(hash) % dimensions;
            float sign = ((hash & 1) == 0) ? 1.0f : -1.0f;
            vec[bucket] += sign * 0.8f;
        }

        // L2 normalization
        double sumSq = 0.0;
        for (float v : vec) {
            sumSq += v * v;
        }

        double norm = Math.sqrt(sumSq);
        List<Float> result = new ArrayList<>(dimensions);
        if (norm > 0.00001) {
            for (float v : vec) {
                result.add((float) (v / norm));
            }
        } else {
            for (int i = 0; i < dimensions; i++) {
                result.add((float) (1.0 / Math.sqrt(dimensions)));
            }
        }

        return result;
    }
}
