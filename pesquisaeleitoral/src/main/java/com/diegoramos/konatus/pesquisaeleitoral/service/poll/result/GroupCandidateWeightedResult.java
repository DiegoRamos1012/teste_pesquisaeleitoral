package com.diegoramos.konatus.pesquisaeleitoral.service.poll.result;

import java.math.BigDecimal;
import java.util.UUID;

public record GroupCandidateWeightedResult(
        UUID candidateId,
        String candidateName,
        BigDecimal weightedPercentage
) {
}

