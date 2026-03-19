package com.diegoramos.konatus.pesquisaeleitoral.dto;

import java.time.LocalDate;
import java.util.List;

public record PollImportResponseDTO(
        String pollId,
        LocalDate pollDate,
        long weightedPopulation,
        List<CandidateWeightedResultDTO> candidates
) {
}

