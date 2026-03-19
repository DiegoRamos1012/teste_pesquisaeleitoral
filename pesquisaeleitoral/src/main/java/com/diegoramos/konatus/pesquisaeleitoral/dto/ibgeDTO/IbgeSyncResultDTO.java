package com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo da sincronizacao da base IBGE")
public record IbgeSyncResultDTO(
        @Schema(description = "Quantidade de estados criados", example = "0")
        int statesCreated,
        @Schema(description = "Quantidade de estados atualizados", example = "1")
        int statesUpdated,
        @Schema(description = "Quantidade de municipios criados", example = "0")
        int municipalitiesCreated,
        @Schema(description = "Indica se foi disparo manual forcado", example = "true")
        boolean forced
) {
}

