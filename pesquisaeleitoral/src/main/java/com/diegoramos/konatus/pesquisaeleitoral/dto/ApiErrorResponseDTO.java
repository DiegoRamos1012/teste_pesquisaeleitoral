package com.diegoramos.konatus.pesquisaeleitoral.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estrutura padrao de erro retornada pela API")
public record ApiErrorResponseDTO(
        @Schema(description = "Codigo HTTP", example = "400")
        int status,
        @Schema(description = "Titulo resumido do erro", example = "Requisicao invalida")
        String error,
        @Schema(description = "Mensagem detalhada", example = "Linha 2 invalida: candidate_id deve ser UUID")
        String message
) {
}

