package com.ledgerlens.service.analyzer;

import com.ledgerlens.dto.InvestigationAnalysis;
import com.ledgerlens.dto.InvestigationEvidenceDto;

public interface InvestigationAnalyzer {
    InvestigationAnalysis analyze(InvestigationEvidenceDto evidence);
}
