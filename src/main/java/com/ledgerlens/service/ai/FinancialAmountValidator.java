package com.ledgerlens.service.ai;

import com.ledgerlens.dto.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FinancialAmountValidator {

    private static final Pattern CURRENCY_PATTERN = Pattern.compile("(?:₹|Rs\\.?|INR|\\$)\\s*(\\d+(?:\\.\\d+)?)");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\b(\\d+\\.\\d{2})\\b");

    public boolean validateAmounts(String text, InvestigationEvidenceDto evidence) {
        if (text == null || text.isBlank() || evidence == null) {
            return true;
        }

        Set<BigDecimal> validAmounts = extractValidEvidenceAmounts(evidence);

        // Extract currency numbers like ₹500 or Rs. 976.40 or 500.00
        Set<BigDecimal> textAmounts = extractAmountsFromText(text);

        for (BigDecimal textAmount : textAmounts) {
            // Allow zero or trivial small counts (like 1, 2, 100 for percentages)
            if (isTrivialOrNonMonetary(textAmount)) {
                continue;
            }

            boolean matched = validAmounts.stream()
                    .anyMatch(valid -> isEquivalentAmount(valid, textAmount));

            if (!matched) {
                return false; // Invented financial amount detected!
            }
        }

        return true;
    }

    private Set<BigDecimal> extractValidEvidenceAmounts(InvestigationEvidenceDto evidence) {
        Set<BigDecimal> amounts = new HashSet<>();

        if (evidence.getException() != null) {
            addIfNotNull(amounts, evidence.getException().getExpectedAmount());
            addIfNotNull(amounts, evidence.getException().getActualAmount());
            addIfNotNull(amounts, evidence.getException().getDiscrepancyAmount());
        }

        if (evidence.getOrder() != null) {
            addIfNotNull(amounts, evidence.getOrder().getAmount());
        }

        if (evidence.getPayment() != null) {
            addIfNotNull(amounts, evidence.getPayment().getAmount());
        }

        if (evidence.getRefunds() != null) {
            evidence.getRefunds().forEach(r -> addIfNotNull(amounts, r.getAmount()));
        }

        if (evidence.getFees() != null) {
            evidence.getFees().forEach(f -> {
                addIfNotNull(amounts, f.getFeeAmount());
                addIfNotNull(amounts, f.getTaxAmount());
                addIfNotNull(amounts, f.getTotalFee());
            });
        }

        if (evidence.getAdjustments() != null) {
            evidence.getAdjustments().forEach(a -> addIfNotNull(amounts, a.getAmount()));
        }

        if (evidence.getSettlement() != null) {
            addIfNotNull(amounts, evidence.getSettlement().getGrossAmount());
            addIfNotNull(amounts, evidence.getSettlement().getTotalRefundAmount());
            addIfNotNull(amounts, evidence.getSettlement().getTotalFeeAmount());
            addIfNotNull(amounts, evidence.getSettlement().getTotalTaxAmount());
            addIfNotNull(amounts, evidence.getSettlement().getTotalAdjustmentAmount());
            addIfNotNull(amounts, evidence.getSettlement().getExpectedNetAmount());
            addIfNotNull(amounts, evidence.getSettlement().getActualSettledAmount());
        }

        if (evidence.getCalculatedAmounts() != null) {
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getGrossAmount());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getTotalRefunds());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getTotalFees());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getTotalTaxes());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getTotalAdjustments());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getExpectedSettlement());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getActualSettlement());
            addIfNotNull(amounts, evidence.getCalculatedAmounts().getDiscrepancy());
        }

        return amounts;
    }

    private Set<BigDecimal> extractAmountsFromText(String text) {
        Set<BigDecimal> extracted = new HashSet<>();

        Matcher currencyMatcher = CURRENCY_PATTERN.matcher(text);
        while (currencyMatcher.find()) {
            try {
                extracted.add(new BigDecimal(currencyMatcher.group(1)));
            } catch (Exception ignored) {}
        }

        Matcher numericMatcher = NUMERIC_PATTERN.matcher(text);
        while (numericMatcher.find()) {
            try {
                extracted.add(new BigDecimal(numericMatcher.group(1)));
            } catch (Exception ignored) {}
        }

        return extracted;
    }

    private void addIfNotNull(Set<BigDecimal> set, BigDecimal val) {
        if (val != null) {
            set.add(val.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private boolean isEquivalentAmount(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.setScale(2, RoundingMode.HALF_UP).compareTo(b.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    private boolean isTrivialOrNonMonetary(BigDecimal amount) {
        if (amount == null) return true;
        BigDecimal val = amount.setScale(2, RoundingMode.HALF_UP);
        return val.compareTo(BigDecimal.ZERO) == 0 ||
               val.compareTo(new BigDecimal("1.00")) == 0 ||
               val.compareTo(new BigDecimal("100.00")) == 0;
    }

    /**
     * Phase 3: Validates that AI response does not contain fabricated historical cases or evidence.
     * 
     * Backend-supplied historical cases are authoritative. AI should not invent investigation IDs.
     */
    public boolean validatePhase3Response(AiInvestigationResponse response, 
                                         List<HistoricalCaseDto> suppliedHistoricalCases) {
        if (response == null) {
            return true;
        }

        // Validate that AI did not fabricate historical investigation IDs in its text
        List<HistoricalCaseDto> aiHistoricalCases = response.getSimilarHistoricalCases();
        
        if (aiHistoricalCases != null && !aiHistoricalCases.isEmpty()) {
            // AI response should only contain backend-supplied historical cases
            // We don't expect AI to return historical cases in structured form
            // (they're added by backend after AI response)
            // But if it does, they must match supplied ones
            
            for (HistoricalCaseDto aiCase : aiHistoricalCases) {
                boolean matchFound = suppliedHistoricalCases.stream()
                        .anyMatch(supplied -> supplied.getInvestigationId().equals(aiCase.getInvestigationId()));
                
                if (!matchFound) {
                    // AI fabricated a historical investigation ID
                    return false;
                }
            }
        }

        // Confidence score must be 0-100
        BigDecimal confidence = response.getConfidenceScore();
        if (confidence != null && (confidence.compareTo(BigDecimal.ZERO) < 0 || confidence.compareTo(new BigDecimal("100")) > 0)) {
            return false;
        }

        return true;
    }

    /**
     * Phase 3 Forensic Reasoning: Validates forensic reasoning fields
     * 
     * Ensures AI does not fabricate exception IDs in related exceptions
     */
    public boolean validateForensicReasoning(AiInvestigationResponse response,
                                            List<RelatedExceptionDto> suppliedRelatedExceptions) {
        if (response == null) {
            return true;
        }

        // Validate related exception IDs match backend-supplied IDs
        List<RelatedExceptionDto> aiRelatedExceptions = response.getRelatedExceptions();
        if (aiRelatedExceptions != null && !aiRelatedExceptions.isEmpty()) {
            for (RelatedExceptionDto aiRelated : aiRelatedExceptions) {
                boolean matchFound = suppliedRelatedExceptions.stream()
                        .anyMatch(supplied -> supplied.getExceptionId().equals(aiRelated.getExceptionId()));
                
                if (!matchFound) {
                    // AI fabricated a related exception ID
                    return false;
                }
            }
        }

        // Validate hypothesis confidences are 0-100
        List<HypothesisDto> hypotheses = response.getHypotheses();
        if (hypotheses != null) {
            for (HypothesisDto hypothesis : hypotheses) {
                BigDecimal hypConf = hypothesis.getConfidence();
                if (hypConf != null && (hypConf.compareTo(BigDecimal.ZERO) < 0 || hypConf.compareTo(new BigDecimal("100")) > 0)) {
                    return false;
                }
            }
        }

        return true;
    }
}
