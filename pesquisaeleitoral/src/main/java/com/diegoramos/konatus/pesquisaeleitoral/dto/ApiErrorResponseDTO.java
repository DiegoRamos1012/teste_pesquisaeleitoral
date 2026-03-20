package com.diegoramos.konatus.pesquisaeleitoral.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estrutura padrão de erro retornada pela API")
public record ApiErrorResponseDTO(
        @Schema(description = "Codigo HTTP", example = "400")
        int status,
        @Schema(description = "Título resumido do erro", example = "Requisição inválida")
        String error,
        @Schema(description = "Mensagem detalhada", example = "Linha 2 inválida: candidate_id deve ser UUID")
        String message
) {
}

