package com.diegoramos.konatus.pesquisaeleitoral.service.poll.result;

import java.util.List;

public record PollGroupBreakdown(
        String stateAcronym,
        com.diegoramos.konatus.pesquisaeleitoral.service.poll.weighting.MunicipalitySizeGroup municipalityGroup,
        long groupPopulation,
        long sampledPopulation,
        List<GroupCandidateWeightedResult> candidates
) {
}

