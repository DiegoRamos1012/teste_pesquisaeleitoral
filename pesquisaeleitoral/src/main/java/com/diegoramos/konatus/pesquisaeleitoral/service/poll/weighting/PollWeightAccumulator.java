package com.diegoramos.konatus.pesquisaeleitoral.service.poll.weighting;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.CandidateWeightedResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.GroupCandidateWeightedResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollGroupBreakdown;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class PollWeightAccumulator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final Map<UUID, BigDecimal> projectedCandidateVotes = new HashMap<>();
    private final Map<UUID, String> candidateNames = new HashMap<>();
    private final Map<GroupRegionKey, Long> regionTotalPopulation = new HashMap<>();
    private final Map<GroupRegionKey, Long> regionSamplePopulation = new HashMap<>();
    private final Map<GroupRegionKey, Map<UUID, BigDecimal>> regionSampleWeightedVotes = new HashMap<>();
    private final Map<GroupRegionKey, Map<UUID, String>> regionSampleCandidateNames = new HashMap<>();
    private final Map<GroupRegionKey, Map<UUID, Integer>> seenMunicipalities = new HashMap<>();
    private final Map<GroupRegionKey, String> regionStateAcronyms = new HashMap<>();
    private final Map<GroupRegionKey, Map<UUID, BigDecimal>> regionProjectedCandidatePercentages = new HashMap<>();

    public void registerStateMunicipalities(State state, Collection<Municipality> municipalities) {
        Map<MunicipalitySizeGroup, Long> groupedPopulation = new HashMap<>();

        for (Municipality municipality : municipalities) {
            MunicipalitySizeGroup group = MunicipalitySizeGroup.fromPopulation(municipality.getPopulation());
            groupedPopulation.merge(group, (long) municipality.getPopulation(), Long::sum);
        }

        for (Map.Entry<MunicipalitySizeGroup, Long> entry : groupedPopulation.entrySet()) {
            GroupRegionKey key = GroupRegionKey.of(state, entry.getKey());
            regionTotalPopulation.put(key, entry.getValue());
            regionStateAcronyms.put(key, state.getStateAcronym());
        }
    }

    public void addSample(State state, Municipality municipality, Candidate candidate, BigDecimal percentage) {
        GroupRegionKey key = GroupRegionKey.of(state, MunicipalitySizeGroup.fromPopulation(municipality.getPopulation()));

        Map<UUID, Integer> municipalitiesByCandidate = seenMunicipalities.computeIfAbsent(key, ignored -> new HashMap<>());
        municipalitiesByCandidate.putIfAbsent(municipality.getId(), municipality.getPopulation());
        long samplePopulation = municipalitiesByCandidate.values().stream().mapToLong(Integer::longValue).sum();
        regionSamplePopulation.put(key, samplePopulation);

        BigDecimal weightedValue = percentage
                .multiply(BigDecimal.valueOf(municipality.getPopulation()))
                .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);

        regionSampleWeightedVotes
                .computeIfAbsent(key, ignored -> new HashMap<>())
                .merge(candidate.getId(), weightedValue, BigDecimal::add);

        regionSampleCandidateNames
                .computeIfAbsent(key, ignored -> new HashMap<>())
                .putIfAbsent(candidate.getId(), candidate.getName());

        candidateNames.putIfAbsent(candidate.getId(), candidate.getName());
    }

    public long totalPopulation() {
        return regionTotalPopulation.values().stream().mapToLong(Long::longValue).sum();
    }

    public List<CandidateWeightedResult> buildResults(long totalPopulation) {
        projectVotesByRegion();

        List<CandidateWeightedResult> weightedResults = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : projectedCandidateVotes.entrySet()) {
            BigDecimal result = entry.getValue()
                    .divide(BigDecimal.valueOf(totalPopulation), 8, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);

            String candidateName = candidateNames.getOrDefault(entry.getKey(), "Candidato sem nome");
            weightedResults.add(new CandidateWeightedResult(entry.getKey(), candidateName, result));
        }

        return weightedResults;
    }

    public List<PollGroupBreakdown> buildGroupBreakdown() {
        List<PollGroupBreakdown> result = new ArrayList<>();

        for (Map.Entry<GroupRegionKey, Map<UUID, BigDecimal>> regionEntry : regionProjectedCandidatePercentages.entrySet()) {
            GroupRegionKey region = regionEntry.getKey();
            List<GroupCandidateWeightedResult> candidates = regionEntry.getValue().entrySet().stream()
                    .map(entry -> new GroupCandidateWeightedResult(
                            entry.getKey(),
                            candidateNames.getOrDefault(entry.getKey(), "Candidato sem nome"),
                            entry.getValue().setScale(2, RoundingMode.HALF_UP)
                    ))
                    .sorted(Comparator.comparing(GroupCandidateWeightedResult::weightedPercentage).reversed())
                    .toList();

            result.add(new PollGroupBreakdown(
                    regionStateAcronyms.getOrDefault(region, "?"),
                    region.group(),
                    regionTotalPopulation.getOrDefault(region, 0L),
                    regionSamplePopulation.getOrDefault(region, 0L),
                    candidates
            ));
        }

        result.sort(Comparator
                .comparing(PollGroupBreakdown::stateAcronym)
                .thenComparing(item -> item.municipalityGroup().ordinal()));
        return result;
    }

    private void projectVotesByRegion() {
        projectedCandidateVotes.clear();
        regionProjectedCandidatePercentages.clear();

        for (Map.Entry<GroupRegionKey, Map<UUID, BigDecimal>> regionEntry : regionSampleWeightedVotes.entrySet()) {
            GroupRegionKey region = regionEntry.getKey();
            long samplePopulation = regionSamplePopulation.getOrDefault(region, 0L);
            long regionPopulation = regionTotalPopulation.getOrDefault(region, samplePopulation);

            if (samplePopulation <= 0 || regionPopulation <= 0) {
                continue;
            }

            for (Map.Entry<UUID, BigDecimal> candidateEntry : regionEntry.getValue().entrySet()) {
                BigDecimal samplePercentage = candidateEntry.getValue()
                        .divide(BigDecimal.valueOf(samplePopulation), 8, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);

                regionProjectedCandidatePercentages
                        .computeIfAbsent(region, ignored -> new HashMap<>())
                        .put(candidateEntry.getKey(), samplePercentage);

                BigDecimal projectedVotes = samplePercentage
                        .multiply(BigDecimal.valueOf(regionPopulation))
                        .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);

                projectedCandidateVotes.merge(candidateEntry.getKey(), projectedVotes, BigDecimal::add);

                String sampledName = regionSampleCandidateNames
                        .getOrDefault(region, Map.of())
                        .get(candidateEntry.getKey());
                if (sampledName != null) {
                    candidateNames.putIfAbsent(candidateEntry.getKey(), sampledName);
                }
            }
        }
    }

    private record GroupRegionKey(UUID stateId, MunicipalitySizeGroup group) {
        static GroupRegionKey of(State state, MunicipalitySizeGroup group) {
            return new GroupRegionKey(state.getId(), group);
        }
    }
}

