package com.diegoramos.konatus.pesquisaeleitoral.service.poll.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Resultado da importação da pesquisa eleitoral")
public record PollImportResult(
        @Schema(description = "Identificador da pesquisa", example = "PESQ-2026-01")
        String pollId,
        @Schema(description = "Data da pesquisa", example = "2026-03-01")
        LocalDate pollDate,
        @Schema(description = "População total utilizada na ponderação", example = "12700000")
        long weightedPopulation,
        @Schema(description = "Lista de candidatos com percentual ponderado")
        List<CandidateWeightedResult> weightedCandidateResults,
        @Schema(description = "Detalhamento por estado e grupo de porte de município")
        List<PollGroupBreakdown> groupBreakdown
) {
}

