package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import java.time.LocalDate;
import java.util.List;

public record PollImportResult(
        String pollId,
        LocalDate pollDate,
        long weightedPopulation,
        List<CandidateWeightedResult> weightedCandidateResults
) {
}

