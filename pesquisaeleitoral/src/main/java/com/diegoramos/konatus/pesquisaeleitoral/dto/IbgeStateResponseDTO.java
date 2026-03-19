package com.diegoramos.konatus.pesquisaeleitoral.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IbgeStateResponseDTO(
        Long id,
        String sigla,
        String nome
) {
}
