package com.ledgerlens;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerlens.config.AiProperties;
import com.ledgerlens.dto.*;
import com.ledgerlens.entity.enums.ExceptionType;
import com.ledgerlens.entity.enums.RecommendedAction;
import com.ledgerlens.service.ai.FinancialAmountValidator;
import com.ledgerlens.service.ai.GeminiAiInvestigationAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiInvestigationAnalyzerTest {

    private AiProperties aiProperties;
    private ObjectMapper objectMapper;
    private GeminiAiInvestigationAnalyzer geminiAnalyzer;
    private FinancialAmountValidator amountValidator;

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setEnabled(true);
        aiProperties.setApiKey("test-key-123");
        aiProperties.setModel("gemini-1.5-flash");
        aiProperties.setBaseUrl("https://generativelanguage.googleapis.com");

        objectMapper = new ObjectMapper();
        geminiAnalyzer = new GeminiAiInvestigationAnalyzer(aiProperties, objectMapper);
        geminiAnalyzer.setRestClient(restClient);

        amountValidator = new FinancialAmountValidator();
    }

    @Test
    void testSuccessfulAiResponse() {
        String mockGeminiResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "{\\"summary\\": \\"Settlement payout is ₹500.00 lower than expected.\\", \\"likelyRootCause\\": \\"Unexplained payout gap of ₹500.00.\\", \\"supportingEvidence\\": \\"Gross 1000.00, net 976.40, settled 476.40.\\", \\"confidenceScore\\": 96.0, \\"recommendedAction\\": \\"HUMAN_REVIEW_REQUIRED\\", \\"actionTaken\\": \\"SENT_TO_HUMAN\\", \\"autoResolved\\": false, \\"requiresHumanReview\\": true, \\"missingEvidence\\": \\"None\\"}"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(mockGeminiResponse);

        InvestigationEvidenceDto evidence = createSampleEvidence();
        InvestigationAnalysis deterministic = InvestigationAnalysis.builder()
                .summary("Rule summary")
                .likelyRootCause("Rule root cause")
                .recommendedAction(RecommendedAction.HUMAN_REVIEW_REQUIRED)
                .build();

        AiInvestigationResponse response = geminiAnalyzer.analyzeWithAi(evidence, deterministic);

        assertNotNull(response);
        assertEquals("Settlement payout is ₹500.00 lower than expected.", response.getSummary());
        assertEquals("Unexplained payout gap of ₹500.00.", response.getLikelyRootCause());
        assertEquals(new BigDecimal("96.0"), response.getConfidenceScore());
        assertEquals(RecommendedAction.HUMAN_REVIEW_REQUIRED, response.getRecommendedAction());
        assertFalse(response.isAutoResolved());
    }

    @Test
    void testMalformedAiResponse() {
        String mockMalformedResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "NOT_A_VALID_JSON_STRING"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(mockMalformedResponse);

        InvestigationEvidenceDto evidence = createSampleEvidence();

        assertThrows(RuntimeException.class, () -> geminiAnalyzer.analyzeWithAi(evidence, null));
    }

    @Test
    void testAiFailureFallback() {
        when(restClient.post()).thenThrow(new RuntimeException("Connection timeout"));

        InvestigationEvidenceDto evidence = createSampleEvidence();

        assertThrows(RuntimeException.class, () -> geminiAnalyzer.analyzeWithAi(evidence, null));
    }

    @Test
    void testMissingApiKeyFallback() {
        aiProperties.setApiKey(""); // Missing key

        InvestigationEvidenceDto evidence = createSampleEvidence();

        assertThrows(IllegalStateException.class, () -> geminiAnalyzer.analyzeWithAi(evidence, null));
    }

    @Test
    void testPreservationOfExactFinancialAmounts() {
        InvestigationEvidenceDto evidence = createSampleEvidence();

        // Valid amounts present in evidence (500.00, 976.40, 476.40)
        String validText = "Settlement shortfall of ₹500.00 from expected net ₹976.40 to actual ₹476.40";
        assertTrue(amountValidator.validateAmounts(validText, evidence));

        // Invalid / invented amount (₹9999.99 is NOT in evidence)
        String inventedText = "Settlement shortfall of ₹9999.99 detected!";
        assertFalse(amountValidator.validateAmounts(inventedText, evidence));
    }

    private InvestigationEvidenceDto createSampleEvidence() {
        InvestigationEvidenceDto.ExceptionSummaryDto ex = new InvestigationEvidenceDto.ExceptionSummaryDto();
        ex.setExceptionId("exp_001");
        ex.setExceptionType(ExceptionType.AMOUNT_MISMATCH);
        ex.setExpectedAmount(new BigDecimal("976.40"));
        ex.setActualAmount(new BigDecimal("476.40"));
        ex.setDiscrepancyAmount(new BigDecimal("500.00"));

        InvestigationEvidenceDto.SettlementSummaryDto set = new InvestigationEvidenceDto.SettlementSummaryDto();
        set.setSettlementId("set_1002");
        set.setGrossAmount(new BigDecimal("1000.00"));
        set.setExpectedNetAmount(new BigDecimal("976.40"));
        set.setActualSettledAmount(new BigDecimal("476.40"));

        InvestigationEvidenceDto.CalculatedAmountsDto calc = new InvestigationEvidenceDto.CalculatedAmountsDto();
        calc.setGrossAmount(new BigDecimal("1000.00"));
        calc.setExpectedSettlement(new BigDecimal("976.40"));
        calc.setActualSettlement(new BigDecimal("476.40"));
        calc.setDiscrepancy(new BigDecimal("500.00"));

        return InvestigationEvidenceDto.builder()
                .exception(ex)
                .settlement(set)
                .calculatedAmounts(calc)
                .build();
    }
}
