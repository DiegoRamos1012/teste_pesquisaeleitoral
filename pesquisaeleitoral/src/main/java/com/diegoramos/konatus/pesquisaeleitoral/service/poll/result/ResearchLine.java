package com.diegoramos.konatus.pesquisaeleitoral.service.poll.result;

import java.math.BigDecimal;
import java.util.UUID;

public record ResearchLine(
        String pollId,
        java.time.LocalDate pollDate,
        String state,
        String municipality,
        UUID candidateId,
        BigDecimal percentual
) {
}

