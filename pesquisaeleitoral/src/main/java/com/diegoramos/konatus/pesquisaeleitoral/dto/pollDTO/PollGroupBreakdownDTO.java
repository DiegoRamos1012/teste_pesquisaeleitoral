package com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO;

import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.GroupCandidateWeightedResultDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detalhamento da pesquisa por estado e grupo de porte")
public record PollGroupBreakdownDTO(
        @Schema(description = "Sigla do estado", example = "SP")
        String stateAcronym,
        @Schema(description = "Grupo de porte do município", example = "GROUP_4")
        String municipalityGroup,
        @Schema(description = "População total do grupo no estado", example = "12300000")
        long groupPopulation,
        @Schema(description = "População coberta pela amostra no grupo", example = "3450000")
        long sampledPopulation,
        @ArraySchema(schema = @Schema(implementation = GroupCandidateWeightedResultDTO.class,
                description = "Candidatos no recorte estado + porte"))
        List<GroupCandidateWeightedResultDTO> candidates
) {
}

