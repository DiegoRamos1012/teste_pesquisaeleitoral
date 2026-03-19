package com.diegoramos.konatus.pesquisaeleitoral.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CandidateWeightedResultDTO(
        UUID candidateId,
        String candidateName,
        BigDecimal weightedPercentage
) {
}

