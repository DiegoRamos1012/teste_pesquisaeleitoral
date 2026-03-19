package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ResearchLine(
        String pollId,
        LocalDate pollDate,
        String state,
        String municipality,
        UUID candidateId,
        BigDecimal percentual
) {
}

