package com.diegoramos.konatus.pesquisaeleitoral.dto;

public record IbgeSyncResultDTO(
        int statesCreated,
        int statesUpdated,
        int municipalitiesCreated,
        boolean forced
) {
}

