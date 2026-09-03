package com.ledgerlens.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.*;
import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.RecommendedAction;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GeminiAiInvestigationAnalyzer implements AiInvestigationAnalyzer {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private RestClient restClient;

    public GeminiAiInvestigationAnalyzer(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    // Set custom RestClient for testing or default initialization
    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    private RestClient getRestClient() {
        if (this.restClient == null) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            int timeout = aiProperties.getTimeoutMs() > 0 ? aiProperties.getTimeoutMs() : 5000;
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
            this.restClient = RestClient.builder()
                    .requestFactory(requestFactory)
                    .build();
        }
        return this.restClient;
    }

    @Override
    public AiInvestigationResponse analyzeWithAi(InvestigationEvidenceDto evidence, InvestigationAnalysis deterministicAnalysis) {
        return analyzeWithAi(evidence, deterministicAnalysis, List.of(), List.of(), List.of());
    }

    /**
     * Phase 3: Enhanced AI analysis with historical cases and anomaly detection
     * Phase 3 Forensic Reasoning: With competing hypotheses, contradictions, and evidence requests
     * Phase 3.5: Evidence graph and sufficiency (overload for backward compatibility)
     */
    public AiInvestigationResponse analyzeWithAi(InvestigationEvidenceDto evidence, 
                                                InvestigationAnalysis deterministicAnalysis,
                                                List<HistoricalCaseDto> historicalCases,
                                                List<SoftAnomalyDto> anomalies,
                                                List<RelatedExceptionDto> relatedExceptions) {
        return analyzeWithAi(evidence, deterministicAnalysis, historicalCases, anomalies, relatedExceptions, null, null, List.of());
    }

    /**
     * Phase 3.5: Evidence Graph + Evidence Sufficiency (overload for backward compatibility)
     */
    public AiInvestigationResponse analyzeWithAi(InvestigationEvidenceDto evidence, 
                                                InvestigationAnalysis deterministicAnalysis,
                                                List<HistoricalCaseDto> historicalCases,
                                                List<SoftAnomalyDto> anomalies,
                                                List<RelatedExceptionDto> relatedExceptions,
                                                EvidenceGraphDto evidenceGraph,
                                                EvidenceSufficiencyDto evidenceSufficiency) {
        return analyzeWithAi(evidence, deterministicAnalysis, historicalCases, anomalies, relatedExceptions, evidenceGraph, evidenceSufficiency, List.of());
    }

    /**
     * Phase 6: Hybrid Financial RAG + Evidence Graph + Evidence Sufficiency
     * AI receives authoritative evidence graph, deterministic historical matches, and hybrid RAG semantic matches
     */
    public AiInvestigationResponse analyzeWithAi(InvestigationEvidenceDto evidence, 
                                                InvestigationAnalysis deterministicAnalysis,
                                                List<HistoricalCaseDto> historicalCases,
                                                List<SoftAnomalyDto> anomalies,
                                                List<RelatedExceptionDto> relatedExceptions,
                                                EvidenceGraphDto evidenceGraph,
                                                EvidenceSufficiencyDto evidenceSufficiency,
                                                List<RagHistoricalCaseDto> ragHistoricalCases) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("AI API key is missing or blank");
        }

        try {
            String evidenceJson = objectMapper.writeValueAsString(evidence);
            String historicalJson = (historicalCases == null || historicalCases.isEmpty()) ? "None available" : objectMapper.writeValueAsString(historicalCases);
            String ragHistoricalJson = (ragHistoricalCases == null || ragHistoricalCases.isEmpty()) ? "None available" : objectMapper.writeValueAsString(ragHistoricalCases);
            String anomalyJson = (anomalies == null || anomalies.isEmpty()) ? "None detected" : objectMapper.writeValueAsString(anomalies);
            String relatedJson = (relatedExceptions == null || relatedExceptions.isEmpty()) ? "None identified" : objectMapper.writeValueAsString(relatedExceptions);
            
            // Phase 3.5: Include evidence graph and sufficiency
            String graphJson = evidenceGraph != null ? objectMapper.writeValueAsString(evidenceGraph) : "Not available (legacy mode)";
            String sufficiencyJson = evidenceSufficiency != null ? objectMapper.writeValueAsString(evidenceSufficiency) : "Not calculated (legacy mode)";
            
            String promptText = buildPromptText(evidenceJson, deterministicAnalysis, historicalJson, ragHistoricalJson, anomalyJson, relatedJson, graphJson, sufficiencyJson);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", java.util.List.of(
                            Map.of("parts", java.util.List.of(
                                    Map.of("text", promptText)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json"
                    )
            ));

            String url = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                    aiProperties.getBaseUrl(),
                    aiProperties.getModel(),
                    aiProperties.getApiKey());

            String rawResponseBody = getRestClient().post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGeminiResponse(rawResponseBody, historicalCases, anomalies, relatedExceptions, ragHistoricalCases);

        } catch (Exception e) {
            throw new RuntimeException("Gemini AI analysis failed: " + e.getMessage(), e);
        }
    }

    private String buildPromptText(String evidenceJson, InvestigationAnalysis deterministicAnalysis, 
                                  String historicalJson, String ragHistoricalJson, String anomalyJson, String relatedJson,
                                  String evidenceGraphJson, String evidenceSufficiencyJson) {
        return """
                You are FinSight AI Forensic Financial Investigator (Phase 6 Hybrid Financial RAG Complete).
                
                YOUR ROLE:
                You investigate financial discrepancies by comparing competing hypotheses, identifying contradictions,
                and determining what evidence is missing. You do NOT determine financial truth — the deterministic
                backend does that. You receive verified financial facts and reason about their implications.

                STRICT ARCHITECTURAL CONSTRAINTS:
                1. DETERMINISTIC BACKEND = FINANCIAL TRUTH (amounts, transactions, reconciliation)
                2. AI = FORENSIC INVESTIGATOR (hypotheses, contradictions, evidence analysis)
                3. Do NOT invent financial amounts, transaction IDs, investigation IDs, or evidence
                4. Do NOT override deterministic calculations
                5. You are ADVISORY ONLY - cannot modify financial records
                6. You CANNOT execute refunds, payments, or settlements
                7. You CANNOT bypass merchant isolation
                8. All recommendations require human approval

                PHASE 3.5 & PHASE 6 EVIDENCE RULES (CRITICAL):
                9. Never invent evidence that is not in SECTION G (Evidence Graph)
                10. Never invent transaction IDs, exception IDs, or entity IDs
                11. Never invent relationships between entities
                12. Never treat MISSING evidence as zero or assume missing = non-existent
                13. When evidence is MISSING, state that explicitly in your analysis
                14. Ground ALL hypotheses in FOUND evidence from the graph
                15. If evidence sufficiency is INSUFFICIENT or PARTIAL, acknowledge uncertainty
                16. Every financial fact you cite MUST have a source from the Evidence Graph
                17. Distinguish between AUTHORITATIVE FACT (from backend) and AI REASONING
                18. Current Evidence Graph data (SECTION G) is AUTHORITATIVE. Historical cases (both deterministic in SECTION C1 and semantic RAG in SECTION C2) are reference context only and must never be treated as evidence about the current transaction's actual amounts, entities, or status.

                JSON Output Format Required:
                {
                  "summary": "<Brief investigation summary>",
                  "likelyRootCause": "<Most probable root cause based on strongest hypothesis>",
                  "supportingEvidence": "<Key evidence supporting the conclusion>",
                  "confidenceScore": 85.0,
                  "recommendedAction": "HUMAN_REVIEW_REQUIRED",
                  "actionTaken": "SENT_TO_HUMAN",
                  "autoResolved": false,
                  "requiresHumanReview": true,
                  "missingEvidence": "Settlement mapping",
                  "uncertaintyNotes": "<What remains uncertain>",
                  "limitations": "<What could not be determined>",
                  "hypotheses": [
                    {
                      "hypothesis": "<Plausible explanation>",
                      "confidence": 85.0,
                      "supportingEvidence": ["<fact 1 WITH SOURCE>", "<fact 2 WITH SOURCE>"],
                      "contradictingEvidence": ["<counter-fact WITH SOURCE>"],
                      "status": "SUPPORTED | WEAKENED | CONTRADICTED | UNRESOLVED"
                    }
                  ],
                  "contradictions": [
                    {
                      "contradiction": "<Description of conflict>",
                      "evidenceA": "<First piece of evidence WITH SOURCE>",
                      "evidenceB": "<Conflicting evidence WITH SOURCE>",
                      "severity": "LOW | MEDIUM | HIGH | CRITICAL",
                      "resolution": "<How to resolve or which evidence is stronger>",
                      "unresolved": false
                    }
                  ],
                  "additionalEvidenceRequired": [
                    {
                      "evidenceType": "SETTLEMENT_MAPPING | PAYMENT_GATEWAY_LOG | etc",
                      "description": "<Specific evidence needed>",
                      "reason": "<Why this evidence would help>",
                      "expectedImpact": "<What it would clarify>"
                    }
                  ]
                }

                SECTION A: AUTHORITATIVE DETERMINISTIC FINDINGS
                Rule-Based Summary: %s
                Rule-Based Root Cause: %s
                Rule-Based Action: %s

                SECTION B: VERIFIED FINANCIAL EVIDENCE (Legacy Format)
                %s

                SECTION C1: DETERMINISTIC HISTORICAL MATCHES (Backend Exact Signals, Reference Context Only)
                %s

                SECTION C2: HYBRID RAG HISTORICAL MATCHES (Semantic & Vector Retrieved, Reference Context Only)
                %s

                SECTION D: DETERMINISTIC ANOMALY SIGNALS (Backend-Supplied, DO NOT RECALCULATE)
                %s

                SECTION E: RELATED EXCEPTIONS (Backend-Supplied, DO NOT FABRICATE IDs)
                %s

                SECTION G: EVIDENCE GRAPH (Phase 3.5 - AUTHORITATIVE SOURCE WITH PROVENANCE)
                This is the COMPLETE list of evidence retrieved by the backend.
                Every node shows its source (database table), availability status, and relationship.
                
                CRITICAL: If evidence is marked MISSING, it is ACTUALLY MISSING - do not assume it exists.
                CRITICAL: If evidence is marked FOUND, its details are AUTHORITATIVE - do not modify amounts.
                CRITICAL: You MUST cite the source when referencing evidence (e.g., "Payment P123 from payments table").
                
                %s

                SECTION H: EVIDENCE SUFFICIENCY (Phase 3.5 - BACKEND CALCULATED)
                This is a DETERMINISTIC assessment of whether enough evidence exists for investigation.
                The backend calculates this score - you do NOT recalculate it.
                
                If sufficiency is INSUFFICIENT or PARTIAL, acknowledge this limits your confidence.
                Missing evidence should be explicitly noted in your additionalEvidenceRequired section.
                
                %s

                SECTION I: AI FORENSIC INVESTIGATION TASK
                
                Using ONLY the evidence from SECTION G (Evidence Graph):
                
                1. Generate 2-4 competing hypotheses that explain the financial exception
                2. For each hypothesis, cite SPECIFIC evidence nodes from the graph (with their source)
                3. Mark hypothesis status based on evidence availability:
                   - SUPPORTED: Strong evidence from FOUND nodes
                   - WEAKENED: Some supporting evidence, but contradictions exist
                   - CONTRADICTED: Evidence disproves the hypothesis
                   - UNRESOLVED: MISSING evidence prevents confirmation
                
                4. Identify contradictions between evidence nodes
                5. For contradictions involving DETERMINISTIC vs AI reasoning: DETERMINISTIC WINS
                
                6. Request additional evidence:
                   - Be SPECIFIC about what's missing (reference MISSING nodes from graph)
                   - Explain how it would resolve ambiguity
                   - Reference evidence sufficiency score from SECTION H
                
                7. Consider evidence sufficiency when setting confidenceScore:
                   - INSUFFICIENT sufficiency → confidence should reflect high uncertainty
                   - PARTIAL sufficiency → moderate confidence, note limitations
                   - SUFFICIENT sufficiency → confidence based on hypothesis strength
                
                8. In your summary, distinguish:
                   - AUTHORITATIVE FACTS (from Evidence Graph with source citations)
                   - AI REASONING (your logical deductions)
                   - MISSING EVIDENCE (explicitly noted gaps)
                
                Remember: 
                - You investigate money. You do not control money.
                - You reason over evidence. You do not create evidence.
                - Missing evidence ≠ zero. Missing evidence = unknown.
                - Every financial claim needs a source from the Evidence Graph.
                """.formatted(
                deterministicAnalysis != null ? deterministicAnalysis.getSummary() : "N/A",
                deterministicAnalysis != null ? deterministicAnalysis.getLikelyRootCause() : "N/A",
                deterministicAnalysis != null ? deterministicAnalysis.getRecommendedAction() : "N/A",
                evidenceJson,
                historicalJson,
                ragHistoricalJson,
                anomalyJson,
                relatedJson,
                evidenceGraphJson,
                evidenceSufficiencyJson
        );
    }

    private AiInvestigationResponse parseGeminiResponse(String responseJson, 
                                                        List<HistoricalCaseDto> historicalCases,
                                                        List<SoftAnomalyDto> anomalies,
                                                        List<RelatedExceptionDto> relatedExceptions) throws Exception {
        return parseGeminiResponse(responseJson, historicalCases, anomalies, relatedExceptions, List.of());
    }

    private AiInvestigationResponse parseGeminiResponse(String responseJson, 
                                                        List<HistoricalCaseDto> historicalCases,
                                                        List<SoftAnomalyDto> anomalies,
                                                        List<RelatedExceptionDto> relatedExceptions,
                                                        List<RagHistoricalCaseDto> ragHistoricalCases) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode candidates = root.path("candidates");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Gemini response contained no candidates");
        }

        String jsonText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        JsonNode aiNode = objectMapper.readTree(jsonText);

        String summary = aiNode.path("summary").asText("AI analysis completed");
        String likelyRootCause = aiNode.path("likelyRootCause").asText("Unexplained financial discrepancy");
        String supportingEvidence = aiNode.path("supportingEvidence").asText("");
        double confidence = aiNode.path("confidenceScore").asDouble(90.0);
        // Public investigation scores are percentages. Accept model responses in either 0..1 or 0..100 form.
        if (confidence >= 0.0 && confidence <= 1.0) {
            confidence = confidence * 100.0;
        }

        String recActionStr = aiNode.path("recommendedAction").asText("HUMAN_REVIEW_REQUIRED");
        RecommendedAction recommendedAction;
        try {
            recommendedAction = RecommendedAction.valueOf(recActionStr);
        } catch (Exception e) {
            recommendedAction = RecommendedAction.HUMAN_REVIEW_REQUIRED;
        }

        String actTakenStr = aiNode.path("actionTaken").asText("SENT_TO_HUMAN");
        ActionTaken actionTaken;
        try {
            actionTaken = ActionTaken.valueOf(actTakenStr);
        } catch (Exception e) {
            actionTaken = ActionTaken.SENT_TO_HUMAN;
        }

        boolean autoResolved = aiNode.path("autoResolved").asBoolean(false);
        boolean requiresHumanReview = aiNode.path("requiresHumanReview").asBoolean(true);
        String missingEvidence = aiNode.path("missingEvidence").asText("None");
        
        // Phase 3: Parse existing fields
        String uncertaintyNotes = aiNode.path("uncertaintyNotes").asText("");
        String limitations = aiNode.path("limitations").asText("");

        // Phase 3 Forensic Reasoning: Parse new structured fields
        List<HypothesisDto> hypotheses = parseHypotheses(aiNode.path("hypotheses"));
        List<ContradictionDto> contradictions = parseContradictions(aiNode.path("contradictions"));
        List<EvidenceRequestDto> evidenceRequests = parseEvidenceRequests(aiNode.path("additionalEvidenceRequired"));

        return AiInvestigationResponse.builder()
                .summary(summary)
                .likelyRootCause(likelyRootCause)
                .supportingEvidence(supportingEvidence)
                .confidenceScore(BigDecimal.valueOf(confidence))
                .recommendedAction(recommendedAction)
                .actionTaken(actionTaken)
                .autoResolved(autoResolved)
                .requiresHumanReview(requiresHumanReview)
                .missingEvidence(missingEvidence)
                .similarHistoricalCases(historicalCases)  // Backend-supplied, not from AI
                .softAnomalies(anomalies)  // Backend-supplied, not from AI
                .uncertaintyNotes(uncertaintyNotes)
                .limitations(limitations)
                .hypotheses(hypotheses)
                .contradictions(contradictions)
                .additionalEvidenceRequired(evidenceRequests)
                .relatedExceptions(relatedExceptions)  // Backend-supplied, not from AI
                .ragHistoricalCases(ragHistoricalCases)  // Backend-supplied RAG cases
                .build();
    }

    private List<HypothesisDto> parseHypotheses(JsonNode hypothesesNode) {
        List<HypothesisDto> hypotheses = new ArrayList<>();
        if (hypothesesNode != null && hypothesesNode.isArray()) {
            for (JsonNode node : hypothesesNode) {
                try {
                    HypothesisDto hypothesis = HypothesisDto.builder()
                            .hypothesis(node.path("hypothesis").asText())
                            .confidence(BigDecimal.valueOf(node.path("confidence").asDouble(50.0)))
                            .supportingEvidence(parseStringList(node.path("supportingEvidence")))
                            .contradictingEvidence(parseStringList(node.path("contradictingEvidence")))
                            .status(parseHypothesisStatus(node.path("status").asText("UNRESOLVED")))
                            .build();
                    hypotheses.add(hypothesis);
                } catch (Exception e) {
                    // Skip malformed hypothesis
                }
            }
        }
        return hypotheses;
    }

    private List<ContradictionDto> parseContradictions(JsonNode contradictionsNode) {
        List<ContradictionDto> contradictions = new ArrayList<>();
        if (contradictionsNode != null && contradictionsNode.isArray()) {
            for (JsonNode node : contradictionsNode) {
                try {
                    ContradictionDto contradiction = ContradictionDto.builder()
                            .contradiction(node.path("contradiction").asText())
                            .evidenceA(node.path("evidenceA").asText())
                            .evidenceB(node.path("evidenceB").asText())
                            .severity(parseContradictionSeverity(node.path("severity").asText("MEDIUM")))
                            .resolution(node.path("resolution").asText(""))
                            .unresolved(node.path("unresolved").asBoolean(true))
                            .build();
                    contradictions.add(contradiction);
                } catch (Exception e) {
                    // Skip malformed contradiction
                }
            }
        }
        return contradictions;
    }

    private List<EvidenceRequestDto> parseEvidenceRequests(JsonNode requestsNode) {
        List<EvidenceRequestDto> requests = new ArrayList<>();
        if (requestsNode != null && requestsNode.isArray()) {
            for (JsonNode node : requestsNode) {
                try {
                    EvidenceRequestDto request = EvidenceRequestDto.builder()
                            .evidenceType(node.path("evidenceType").asText())
                            .description(node.path("description").asText())
                            .reason(node.path("reason").asText())
                            .expectedImpact(node.path("expectedImpact").asText())
                            .build();
                    requests.add(request);
                } catch (Exception e) {
                    // Skip malformed request
                }
            }
        }
        return requests;
    }

    private List<String> parseStringList(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode node : arrayNode) {
                result.add(node.asText());
            }
        }
        return result;
    }

    private HypothesisDto.HypothesisStatus parseHypothesisStatus(String status) {
        try {
            return HypothesisDto.HypothesisStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return HypothesisDto.HypothesisStatus.UNRESOLVED;
        }
    }

    private ContradictionDto.ContradictionSeverity parseContradictionSeverity(String severity) {
        try {
            return ContradictionDto.ContradictionSeverity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            return ContradictionDto.ContradictionSeverity.MEDIUM;
        }
    }
}
