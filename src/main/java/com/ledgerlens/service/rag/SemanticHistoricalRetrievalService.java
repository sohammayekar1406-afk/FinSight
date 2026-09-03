package com.ledgerlens.service.rag;

import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.EvidenceGraphDto;
import com.ledgerlens.dto.RagHistoricalCaseDto;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.HistoricalInvestigationEmbedding;
import com.ledgerlens.repository.HistoricalInvestigationEmbeddingRepository;
import com.ledgerlens.service.MerchantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 6.3: Semantic Historical Retrieval Service
 * 
 * Performs merchant-scoped vector similarity retrieval using pgvector / cosine similarity.
 * Hard security constraints:
 * - merchant_id = current merchant (enforced at query level)
 * - status = RESOLVED
 * - excludes current case's investigation
 * - similarity threshold filtering
 * - non-blocking error resilience
 */
@Service
public class SemanticHistoricalRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(SemanticHistoricalRetrievalService.class);

    private final HistoricalInvestigationEmbeddingRepository embeddingRepository;
    private final HistoricalInvestigationEmbeddingService embeddingService;
    private final HybridHistoricalCaseRanker caseRanker;
    private final MerchantContext merchantContext;
    private final AiProperties aiProperties;

    public SemanticHistoricalRetrievalService(
            HistoricalInvestigationEmbeddingRepository embeddingRepository,
            HistoricalInvestigationEmbeddingService embeddingService,
            HybridHistoricalCaseRanker caseRanker,
            MerchantContext merchantContext,
            AiProperties aiProperties) {
        this.embeddingRepository = embeddingRepository;
        this.embeddingService = embeddingService;
        this.caseRanker = caseRanker;
        this.merchantContext = merchantContext;
        this.aiProperties = aiProperties;
    }

    /**
     * Retrieves and ranks similar historical cases using hybrid semantic and metadata signals.
     *
     * @param currentException Current exception under investigation
     * @param evidenceGraph Evidence graph of the current exception
     * @return List of top ranked RAG historical cases (empty on fallback or no matches)
     */
    public List<RagHistoricalCaseDto> findSimilarResolvedCases(FinancialException currentException, EvidenceGraphDto evidenceGraph) {
        if (currentException == null) {
            return List.of();
        }

        if (!aiProperties.isRagEnabled()) {
            log.debug("RAG is disabled via configuration");
            return List.of();
        }

        String merchantId = merchantContext.merchantId();
        if (merchantId == null || merchantId.isBlank()) {
            log.warn("Cannot perform semantic retrieval without merchant context");
            return List.of();
        }

        try {
            // 1. Build structured query text and generate query embedding
            String evidenceSummary = (evidenceGraph != null && evidenceGraph.getTransactionFlow() != null) 
                    ? evidenceGraph.getTransactionFlow() 
                    : "";
            String queryText = embeddingService.buildQueryText(currentException, evidenceSummary);
            List<Float> queryVector = embeddingService.generateEmbedding(queryText);

            if (queryVector.isEmpty()) {
                log.debug("Empty query vector generated for exception {}", currentException.getExceptionId());
                return List.of();
            }

            // 2. Fetch candidate embeddings with strict merchant isolation and resolution status at SQL level
            List<HistoricalInvestigationEmbedding> candidates = embeddingRepository
                    .findResolvedEmbeddingsByMerchant(merchantId, currentException.getExceptionId());

            if (candidates.isEmpty()) {
                log.debug("No historical embeddings found for merchant {}", merchantId);
                return List.of();
            }

            // 3. Compute cosine similarity and apply threshold filter
            BigDecimal minThreshold = BigDecimal.valueOf(aiProperties.getRagSimilarityThreshold());
            List<HybridHistoricalCaseRanker.CandidateWithSimilarity> filteredCandidates = new ArrayList<>();

            for (HistoricalInvestigationEmbedding candidate : candidates) {
                // Secondary check: verify merchantId matches strictly
                if (!merchantId.equals(candidate.getMerchantId())) {
                    log.error("CRITICAL: Candidate merchantId {} violates current merchant context {}", 
                            candidate.getMerchantId(), merchantId);
                    continue;
                }

                BigDecimal similarity = VectorSimilarityUtil.cosineSimilarity(queryVector, candidate.getEmbedding());
                if (similarity.compareTo(minThreshold) >= 0) {
                    filteredCandidates.add(new HybridHistoricalCaseRanker.CandidateWithSimilarity(candidate, similarity));
                }
            }

            if (filteredCandidates.isEmpty()) {
                log.debug("No historical cases met similarity threshold {} for exception {}", 
                        minThreshold, currentException.getExceptionId());
                return List.of();
            }

            // 4. Hybrid ranking (semantic 50%, type 25%, severity 15%, amount 10%)
            int maxResults = aiProperties.getRagMaxResults() > 0 ? aiProperties.getRagMaxResults() : 3;
            return caseRanker.rankCandidates(currentException, filteredCandidates, maxResults);

        } catch (Exception e) {
            // Fallback requirement: Log RAG failure and gracefully return empty list
            log.warn("Semantic historical retrieval failed for exception {}: {}. Falling back to deterministic retrieval.",
                    currentException.getExceptionId(), e.getMessage());
            return List.of();
        }
    }
}
