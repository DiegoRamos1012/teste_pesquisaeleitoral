package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

class PollWeightAccumulator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final Map<UUID, BigDecimal> weightedCandidateVotes = new HashMap<>();
    private final Map<UUID, String> candidateNames = new HashMap<>();
    private final Map<String, Integer> uniqueMunicipalityPopulation = new HashMap<>();

    void add(State state, Municipality municipality, Candidate candidate, BigDecimal percentage) {
        String municipalityKey = state.getId() + ":" + municipality.getId();
        uniqueMunicipalityPopulation.putIfAbsent(municipalityKey, municipality.getPopulation());

        BigDecimal weightedValue = percentage
                .multiply(BigDecimal.valueOf(municipality.getPopulation()))
                .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);

        weightedCandidateVotes.merge(candidate.getId(), weightedValue, BigDecimal::add);
        candidateNames.putIfAbsent(candidate.getId(), candidate.getName());
    }

    long totalPopulation() {
        return uniqueMunicipalityPopulation.values().stream().mapToLong(Integer::longValue).sum();
    }

    List<CandidateWeightedResult> buildResults(long totalPopulation) {
        List<CandidateWeightedResult> weightedResults = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : weightedCandidateVotes.entrySet()) {
            BigDecimal result = entry.getValue()
                    .divide(BigDecimal.valueOf(totalPopulation), 8, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);

            String candidateName = candidateNames.getOrDefault(entry.getKey(), "Candidato sem nome");
            weightedResults.add(new CandidateWeightedResult(entry.getKey(), candidateName, result));
        }

        return weightedResults;
    }
}


