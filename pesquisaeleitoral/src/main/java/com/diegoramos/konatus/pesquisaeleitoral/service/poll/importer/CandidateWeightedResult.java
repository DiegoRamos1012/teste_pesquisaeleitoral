package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import java.math.BigDecimal;
import java.util.UUID;

public record CandidateWeightedResult(
        UUID candidateId,
        String candidateName,
        BigDecimal weightedPercentage
) {
}


