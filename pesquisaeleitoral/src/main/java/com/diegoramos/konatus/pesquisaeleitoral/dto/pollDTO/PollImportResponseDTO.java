package com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO;

import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.CandidateWeightedResultDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Resultado da importação e consolidação ponderada da pesquisa")
public record PollImportResponseDTO(
        @Schema(description = "Identificador da pesquisa", example = "PESQ-2026-01")
        String pollId,
        @Schema(description = "Data da pesquisa", example = "2026-03-01")
        LocalDate pollDate,
        @Schema(description = "População total usada na ponderação", example = "12700000")
        long weightedPopulation,
        @ArraySchema(schema = @Schema(implementation = CandidateWeightedResultDTO.class,
                description = "Resultados ponderados por candidato"))
        List<CandidateWeightedResultDTO> candidates
) {
}

