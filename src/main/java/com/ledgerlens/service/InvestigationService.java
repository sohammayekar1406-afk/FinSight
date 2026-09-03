package com.ledgerlens.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.*;
import com.ledgerlens.entity.AuditLog;
import com.ledgerlens.entity.FinancialException;
import com.ledgerlens.entity.Investigation;
import com.ledgerlens.entity.enums.ActionTaken;
import com.ledgerlens.entity.enums.ExceptionStatus;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.AuditLogRepository;
import com.ledgerlens.repository.FinancialExceptionRepository;
import com.ledgerlens.repository.InvestigationRepository;
import com.ledgerlens.service.ai.AiInvestigationAnalyzer;
import com.ledgerlens.service.ai.FinancialAmountValidator;
import com.ledgerlens.service.ai.GeminiAiInvestigationAnalyzer;
import com.ledgerlens.service.analyzer.InvestigationAnalyzer;
import com.ledgerlens.service.rag.HistoricalInvestigationEmbeddingService;
import com.ledgerlens.service.rag.SemanticHistoricalRetrievalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InvestigationService {

    private final FinancialExceptionRepository exceptionRepository;
    private final InvestigationRepository investigationRepository;
    private final AuditLogRepository auditLogRepository;
    private final EvidenceCollectionService evidenceCollectionService;
    private final InvestigationAnalyzer ruleBasedAnalyzer;
    private final AiInvestigationAnalyzer aiInvestigationAnalyzer;
    private final FinancialAmountValidator financialAmountValidator;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final MerchantContext merchantContext;
    
    // Phase 3: New services
    private final HistoricalInvestigationService historicalInvestigationService;
    private final FinancialAnomalyService financialAnomalyService;
    private final RelatedExceptionService relatedExceptionService;
    
    // Phase 3.5: Evidence Graph Service
    private final EvidenceGraphService evidenceGraphService;

    // Phase 6: Hybrid RAG Services
    private final HistoricalInvestigationEmbeddingService historicalInvestigationEmbeddingService;
    private final SemanticHistoricalRetrievalService semanticHistoricalRetrievalService;

    public InvestigationService(
            FinancialExceptionRepository exceptionRepository,
            InvestigationRepository investigationRepository,
            AuditLogRepository auditLogRepository,
            EvidenceCollectionService evidenceCollectionService,
            InvestigationAnalyzer ruleBasedAnalyzer,
            AiInvestigationAnalyzer aiInvestigationAnalyzer,
            FinancialAmountValidator financialAmountValidator,
            AiProperties aiProperties,
            ObjectMapper objectMapper,
            MerchantContext merchantContext,
            HistoricalInvestigationService historicalInvestigationService,
            FinancialAnomalyService financialAnomalyService,
            RelatedExceptionService relatedExceptionService,
            EvidenceGraphService evidenceGraphService,
            HistoricalInvestigationEmbeddingService historicalInvestigationEmbeddingService,
            SemanticHistoricalRetrievalService semanticHistoricalRetrievalService) {
        this.exceptionRepository = exceptionRepository;
        this.investigationRepository = investigationRepository;
        this.auditLogRepository = auditLogRepository;
        this.evidenceCollectionService = evidenceCollectionService;
        this.ruleBasedAnalyzer = ruleBasedAnalyzer;
        this.aiInvestigationAnalyzer = aiInvestigationAnalyzer;
        this.financialAmountValidator = financialAmountValidator;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.merchantContext = merchantContext;
        this.historicalInvestigationService = historicalInvestigationService;
        this.financialAnomalyService = financialAnomalyService;
        this.relatedExceptionService = relatedExceptionService;
        this.evidenceGraphService = evidenceGraphService;
        this.historicalInvestigationEmbeddingService = historicalInvestigationEmbeddingService;
        this.semanticHistoricalRetrievalService = semanticHistoricalRetrievalService;
    }

    @Transactional
    public InvestigationResponseDto investigateException(String exceptionId) {
        FinancialException exception = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));

        // Idempotency check: if already investigated, return existing investigation
        Optional<Investigation> existingOpt = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId());
        if (existingOpt.isPresent()) {
            Investigation existing = existingOpt.get();
            InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);
            boolean wasAi = "gemini-1.5-flash".equalsIgnoreCase(existing.getAiModelVersion()) || existing.getAiModelVersion().startsWith("gemini");
            return mapToResponse(existing, evidence, wasAi, wasAi ? "REAL_AI_GEMINI" : "RULE_BASED_FALLBACK");
        }

        // Collect evidence & deterministic baseline
        InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);
        InvestigationAnalysis deterministicAnalysis = ruleBasedAnalyzer.analyze(evidence);

        // Phase 3: Retrieve historical cases and detect anomalies (backend-supplied, merchant-scoped)
        List<HistoricalCaseDto> historicalCases = historicalInvestigationService.findSimilarResolvedInvestigations(exception);
        List<SoftAnomalyDto> anomalies = financialAnomalyService.detectAnomalies(exception);
        
        // Phase 3 Forensic Reasoning: Find related exceptions (backend-supplied, merchant-scoped)
        List<RelatedExceptionDto> relatedExceptions = relatedExceptionService.findRelatedExceptions(exception);
        
        // Phase 3.5: Build evidence graph and calculate sufficiency
        EvidenceGraphDto evidenceGraph = evidenceGraphService.buildEvidenceGraph(evidence, exception.getExceptionType());
        EvidenceSufficiencyDto evidenceSufficiency = evidenceGraphService.calculateSufficiency(evidenceGraph, exception.getExceptionType());

        // Phase 6: Retrieve semantic / hybrid RAG historical cases (backend-supplied, merchant-scoped)
        List<RagHistoricalCaseDto> ragHistoricalCases = semanticHistoricalRetrievalService.findSimilarResolvedCases(exception, evidenceGraph);

        InvestigationAnalysis finalAnalysis = deterministicAnalysis;
        boolean aiUsed = false;
        String analysisSource = "RULE_BASED_FALLBACK";
        String aiModelVersion = "rule-based-v1.0";
        String fallbackReason = null;

        // Attempt Real AI Analysis if enabled and configured
        if (aiProperties.isEnabled() && aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            try {
                // Phase 6: Pass evidence graph, sufficiency, deterministic & RAG historical cases to AI
                AiInvestigationResponse aiResponse;
                if (aiInvestigationAnalyzer instanceof GeminiAiInvestigationAnalyzer gemini) {
                    aiResponse = gemini.analyzeWithAi(evidence, deterministicAnalysis, historicalCases, anomalies, 
                                                     relatedExceptions, evidenceGraph, evidenceSufficiency, ragHistoricalCases);
                } else {
                    aiResponse = aiInvestigationAnalyzer.analyzeWithAi(evidence, deterministicAnalysis);
                }

                String combinedText = aiResponse.getSummary() + " " + aiResponse.getLikelyRootCause() + " " + aiResponse.getSupportingEvidence();
                boolean amountsValid = financialAmountValidator.validateAmounts(combinedText, evidence);

                if (amountsValid) {
                    finalAnalysis = InvestigationAnalysis.builder()
                            .summary(aiResponse.getSummary())
                            .likelyRootCause(aiResponse.getLikelyRootCause())
                            .confidenceScore(aiResponse.getConfidenceScore())
                            .recommendedAction(aiResponse.getRecommendedAction())
                            .actionTaken(aiResponse.getActionTaken())
                            .autoResolved(aiResponse.isAutoResolved())
                            .evidence(evidence)
                            .build();

                    aiUsed = true;
                    analysisSource = "REAL_AI_GEMINI";
                    aiModelVersion = aiProperties.getModel() != null ? aiProperties.getModel() : "gemini-1.5-flash";
                } else {
                    fallbackReason = "Validation error: AI output contained unverified financial amounts";
                }
            } catch (Exception e) {
                fallbackReason = "AI failure: " + e.getMessage();
            }
        } else {
            fallbackReason = "AI disabled or API key missing";
        }

        String evidenceJson;
        try {
            evidenceJson = objectMapper.writeValueAsString(evidence);
        } catch (Exception e) {
            evidenceJson = "{\"lineage\":\"" + evidence.getLineage() + "\"}";
        }

        // Build & save Investigation entity
        Investigation investigation = Investigation.builder()
                .exception(exception)
                .likelyRootCause(finalAnalysis.getLikelyRootCause())
                .confidenceScore(finalAnalysis.getConfidenceScore())
                .recommendedAction(finalAnalysis.getRecommendedAction())
                .actionTaken(finalAnalysis.getActionTaken())
                .autoResolved(finalAnalysis.isAutoResolved())
                .summary(finalAnalysis.getSummary())
                .aiModelVersion(aiModelVersion)
                .supportingEvidence(evidenceJson)
                .build();

        investigationRepository.save(investigation);

        // Update exception status
        if (finalAnalysis.isAutoResolved()) {
            exception.setStatus(ExceptionStatus.RESOLVED_AUTO);
            exception.setResolvedAt(OffsetDateTime.now());
            exceptionRepository.save(exception);
            // Phase 6: Embed auto-resolved investigation
            historicalInvestigationEmbeddingService.embedAndPersistResolvedInvestigation(investigation);
        } else if (exception.getStatus() == ExceptionStatus.OPEN) {
            exception.setStatus(ExceptionStatus.INVESTIGATING);
            exceptionRepository.save(exception);
        }

        // Audit Logging
        if (aiUsed) {
            String auditDetails = String.format(
                    "{\"exceptionId\":\"%s\",\"analysisSource\":\"REAL_AI_GEMINI\",\"aiModel\":\"%s\",\"confidenceScore\":%s,\"recommendedAction\":\"%s\"}",
                    exceptionId,
                    aiModelVersion,
                    finalAnalysis.getConfidenceScore(),
                    finalAnalysis.getRecommendedAction()
            );

            AuditLog auditLog = AuditLog.builder()
                    .entityType("INVESTIGATION")
                    .entityId(investigation.getId())
                    .merchantId(exception.getMerchantId())
                    .action("AI_INVESTIGATION_SUCCESS")
                    .performedBy("GEMINI_AI_INVESTIGATOR")
                    .details(auditDetails)
                    .build();
            auditLogRepository.save(auditLog);
        } else {
            String auditDetails = String.format(
                    "{\"exceptionId\":\"%s\",\"analysisSource\":\"RULE_BASED_FALLBACK\",\"fallbackReason\":\"%s\",\"confidenceScore\":%s,\"recommendedAction\":\"%s\"}",
                    exceptionId,
                    fallbackReason != null ? fallbackReason.replace("\"", "\\\"") : "None",
                    finalAnalysis.getConfidenceScore(),
                    finalAnalysis.getRecommendedAction()
            );

            AuditLog auditLog = AuditLog.builder()
                    .entityType("INVESTIGATION")
                    .entityId(investigation.getId())
                    .merchantId(exception.getMerchantId())
                    .action("AI_INVESTIGATION_FALLBACK")
                    .performedBy("SYSTEM_FALLBACK")
                    .details(auditDetails)
                    .build();
            auditLogRepository.save(auditLog);
        }

        return mapToResponse(investigation, evidence, aiUsed, analysisSource);
    }

    @Transactional(readOnly = true)
    public InvestigationResponseDto getInvestigation(String exceptionId) {
        FinancialException exception = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));

        Investigation investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));

        InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);
        boolean wasAi = "gemini-1.5-flash".equalsIgnoreCase(investigation.getAiModelVersion()) || investigation.getAiModelVersion().startsWith("gemini");
        return mapToResponse(investigation, evidence, wasAi, wasAi ? "REAL_AI_GEMINI" : "RULE_BASED_FALLBACK");
    }

    @Transactional
    public RunInvestigationsResultDto investigateAllOpenExceptions() {
        List<FinancialException> exceptions = exceptionRepository.findByMerchantId(merchantContext.merchantId());

        int exceptionsProcessed = 0;
        int investigationsCreated = 0;
        int alreadyInvestigated = 0;
        int autoResolved = 0;
        int sentToHuman = 0;

        for (FinancialException ex : exceptions) {
            Optional<Investigation> existingOpt = investigationRepository.findByException_ExceptionId(ex.getExceptionId());
            if (existingOpt.isPresent()) {
                alreadyInvestigated++;
            } else {
                exceptionsProcessed++;
                InvestigationResponseDto response = investigateException(ex.getExceptionId());
                investigationsCreated++;
                if (response.isAutoResolved()) {
                    autoResolved++;
                } else {
                    sentToHuman++;
                }
            }
        }

        return RunInvestigationsResultDto.builder()
                .exceptionsProcessed(exceptionsProcessed)
                .investigationsCreated(investigationsCreated)
                .alreadyInvestigated(alreadyInvestigated)
                .autoResolved(autoResolved)
                .sentToHuman(sentToHuman)
                .build();
    }

    @Transactional
    public InvestigationResponseDto resolveExceptionManually(String exceptionId) {
        FinancialException exception = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));

        exception.setStatus(ExceptionStatus.RESOLVED_MANUAL);
        exception.setResolvedAt(OffsetDateTime.now());
        exceptionRepository.save(exception);

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "HUMAN_OPERATOR";

        Optional<Investigation> existingOpt = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId());
        Investigation investigation;

        if (existingOpt.isPresent()) {
            investigation = existingOpt.get();
            investigation.setActionTaken(ActionTaken.MANUALLY_OVERRIDDEN);
            investigationRepository.save(investigation);
        } else {
            InvestigationResponseDto initial = investigateException(exceptionId);
            investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
            investigation.setActionTaken(ActionTaken.MANUALLY_OVERRIDDEN);
            investigationRepository.save(investigation);
        }

        // Phase 6: Embed manually resolved investigation
        historicalInvestigationEmbeddingService.embedAndPersistResolvedInvestigation(investigation);

        InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);

        String auditDetails = String.format(
                "{\"exceptionId\":\"%s\",\"action\":\"RESOLVED_MANUAL\",\"performedBy\":\"%s\",\"resolvedAt\":\"%s\"}",
                exceptionId,
                username,
                exception.getResolvedAt()
        );

        AuditLog auditLog = AuditLog.builder()
                .entityType("INVESTIGATION")
                .entityId(investigation.getId())
                .merchantId(exception.getMerchantId())
                .action("HUMAN_REVIEW_RESOLVED")
                .performedBy(username)
                .details(auditDetails)
                .build();
        auditLogRepository.save(auditLog);

        boolean wasAi = "gemini-1.5-flash".equalsIgnoreCase(investigation.getAiModelVersion()) || investigation.getAiModelVersion().startsWith("gemini");
        return mapToResponse(investigation, evidence, wasAi, wasAi ? "REAL_AI_GEMINI" : "RULE_BASED_FALLBACK");
    }

    @Transactional
    public InvestigationResponseDto approveException(String exceptionId) {
        FinancialException exception = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));

        exception.setStatus(ExceptionStatus.RESOLVED_MANUAL);
        exception.setResolvedAt(OffsetDateTime.now());
        exceptionRepository.save(exception);

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "ADMIN";

        Optional<Investigation> existingOpt = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId());
        Investigation investigation;

        if (existingOpt.isPresent()) {
            investigation = existingOpt.get();
            investigation.setActionTaken(ActionTaken.APPROVED);
            investigationRepository.save(investigation);
        } else {
            investigateException(exceptionId);
            investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
            investigation.setActionTaken(ActionTaken.APPROVED);
            investigationRepository.save(investigation);
        }

        // Phase 6: Embed approved resolved investigation
        historicalInvestigationEmbeddingService.embedAndPersistResolvedInvestigation(investigation);

        InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);

        String auditDetails = String.format(
                "{\"exceptionId\":\"%s\",\"action\":\"APPROVED\",\"performedBy\":\"%s\",\"resolvedAt\":\"%s\"}",
                exceptionId,
                username,
                exception.getResolvedAt()
        );

        AuditLog auditLog = AuditLog.builder()
                .entityType("INVESTIGATION")
                .entityId(investigation.getId())
                .merchantId(exception.getMerchantId())
                .action("HUMAN_REVIEW_APPROVED")
                .performedBy(username)
                .details(auditDetails)
                .build();
        auditLogRepository.save(auditLog);

        boolean wasAi = "gemini-1.5-flash".equalsIgnoreCase(investigation.getAiModelVersion()) || investigation.getAiModelVersion().startsWith("gemini");
        return mapToResponse(investigation, evidence, wasAi, wasAi ? "REAL_AI_GEMINI" : "RULE_BASED_FALLBACK");
    }

    @Transactional
    public InvestigationResponseDto rejectException(String exceptionId) {
        FinancialException exception = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));

        exception.setStatus(ExceptionStatus.RESOLVED_MANUAL);
        exception.setResolvedAt(OffsetDateTime.now());
        exceptionRepository.save(exception);

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "ADMIN";

        Optional<Investigation> existingOpt = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId());
        Investigation investigation;

        if (existingOpt.isPresent()) {
            investigation = existingOpt.get();
            investigation.setActionTaken(ActionTaken.REJECTED);
            investigationRepository.save(investigation);
        } else {
            investigateException(exceptionId);
            investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
            investigation.setActionTaken(ActionTaken.REJECTED);
            investigationRepository.save(investigation);
        }

        InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);

        String auditDetails = String.format(
                "{\"exceptionId\":\"%s\",\"action\":\"REJECTED\",\"performedBy\":\"%s\",\"resolvedAt\":\"%s\"}",
                exceptionId,
                username,
                exception.getResolvedAt()
        );

        AuditLog auditLog = AuditLog.builder()
                .entityType("INVESTIGATION")
                .entityId(investigation.getId())
                .merchantId(exception.getMerchantId())
                .action("HUMAN_REVIEW_REJECTED")
                .performedBy(username)
                .details(auditDetails)
                .build();
        auditLogRepository.save(auditLog);

        boolean wasAi = "gemini-1.5-flash".equalsIgnoreCase(investigation.getAiModelVersion()) || investigation.getAiModelVersion().startsWith("gemini");
        return mapToResponse(investigation, evidence, wasAi, wasAi ? "REAL_AI_GEMINI" : "RULE_BASED_FALLBACK");
    }

    @Transactional
    public InvestigationResponseDto escalateException(String exceptionId) {
        FinancialException exception = exceptionRepository.findByExceptionIdAndMerchantId(exceptionId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Exception " + exceptionId + " was not found"));

        exception.setStatus(ExceptionStatus.ESCALATED);
        exceptionRepository.save(exception);

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName() : "ANALYST";

        Optional<Investigation> existingOpt = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId());
        Investigation investigation;

        if (existingOpt.isPresent()) {
            investigation = existingOpt.get();
            investigation.setActionTaken(ActionTaken.ESCALATED);
            investigationRepository.save(investigation);
        } else {
            investigateException(exceptionId);
            investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
            investigation.setActionTaken(ActionTaken.ESCALATED);
            investigationRepository.save(investigation);
        }

        InvestigationEvidenceDto evidence = evidenceCollectionService.collectEvidence(exception);

        String auditDetails = String.format(
                "{\"exceptionId\":\"%s\",\"action\":\"ESCALATED\",\"performedBy\":\"%s\",\"escalatedAt\":\"%s\"}",
                exceptionId,
                username,
                OffsetDateTime.now()
        );

        AuditLog auditLog = AuditLog.builder()
                .entityType("INVESTIGATION")
                .entityId(investigation.getId())
                .merchantId(exception.getMerchantId())
                .action("HUMAN_REVIEW_ESCALATED")
                .performedBy(username)
                .details(auditDetails)
                .build();
        auditLogRepository.save(auditLog);

        boolean wasAi = "gemini-1.5-flash".equalsIgnoreCase(investigation.getAiModelVersion()) || investigation.getAiModelVersion().startsWith("gemini");
        return mapToResponse(investigation, evidence, wasAi, wasAi ? "REAL_AI_GEMINI" : "RULE_BASED_FALLBACK");
    }

    private InvestigationResponseDto mapToResponse(Investigation inv, InvestigationEvidenceDto evidence, boolean aiUsed, String analysisSource) {
        return InvestigationResponseDto.builder()
                .exceptionId(inv.getException().getExceptionId())
                .investigationId(inv.getId().toString())
                .summary(inv.getSummary())
                .likelyRootCause(inv.getLikelyRootCause())
                .confidenceScore(inv.getConfidenceScore())
                .recommendedAction(inv.getRecommendedAction())
                .actionTaken(inv.getActionTaken())
                .autoResolved(inv.isAutoResolved())
                .aiUsed(aiUsed)
                .analysisSource(analysisSource)
                .investigatedAt(inv.getInvestigatedAt())
                .evidence(evidence)
                .build();
    }
}
