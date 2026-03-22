package com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Percentual ponderado de um candidato dentro de um grupo de porte")
public record GroupCandidateWeightedResultDTO(
        @Schema(description = "ID do candidato", example = "11111111-1111-1111-1111-111111111111")
        UUID candidateId,
        @Schema(description = "Nome do candidato", example = "Maria Silva")
        String candidateName,
        @Schema(description = "Percentual no recorte de estado + porte", example = "41.82")
        BigDecimal weightedPercentage
) {
}

