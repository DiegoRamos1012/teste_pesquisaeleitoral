package com.diegoramos.konatus.pesquisaeleitoral.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IbgeMunicipalityResponseDTO(
        Long id,
        String nome
) {
}

