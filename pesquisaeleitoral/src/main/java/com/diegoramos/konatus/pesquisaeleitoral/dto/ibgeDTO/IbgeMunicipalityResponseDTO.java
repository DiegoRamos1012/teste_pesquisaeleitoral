package com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IbgeMunicipalityResponseDTO(
        Long id,
        String nome
) {
}

