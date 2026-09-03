package com.ledgerlens.service.ai;

import com.ledgerlens.dto.AiInvestigationResponse;
import com.ledgerlens.dto.InvestigationAnalysis;
import com.ledgerlens.dto.InvestigationEvidenceDto;

public interface AiInvestigationAnalyzer {
    AiInvestigationResponse analyzeWithAi(InvestigationEvidenceDto evidence, InvestigationAnalysis deterministicAnalysis);
}
