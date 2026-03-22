package com.diegoramos.konatus.pesquisaeleitoral.service.poll.weighting;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.CandidateWeightedResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PollWeightAccumulatorTest {

    @Test
    void shouldCalculateWeightedAverageCorrectly() {
        PollWeightAccumulator accumulator = new PollWeightAccumulator();

        State state = State.create("Sao Paulo", "SP");
        setId(state, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));

        Municipality municipalityA = Municipality.create("Cidade A", 1_000, state);
        setId(municipalityA, UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));

        Municipality municipalityB = Municipality.create("Cidade B", 3_000, state);
        setId(municipalityB, UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));

        Candidate candidate1 = Candidate.create("Maria", "PA");
        setId(candidate1, UUID.fromString("11111111-1111-1111-1111-111111111111"));

        Candidate candidate2 = Candidate.create("Joao", "PB");
        setId(candidate2, UUID.fromString("22222222-2222-2222-2222-222222222222"));

        accumulator.registerStateMunicipalities(state, List.of(municipalityA, municipalityB));

        accumulator.addSample(state, municipalityA, candidate1, new BigDecimal("60"));
        accumulator.addSample(state, municipalityA, candidate2, new BigDecimal("40"));
        accumulator.addSample(state, municipalityB, candidate1, new BigDecimal("30"));
        accumulator.addSample(state, municipalityB, candidate2, new BigDecimal("70"));

        long totalPopulation = accumulator.totalPopulation();
        List<CandidateWeightedResult> results = accumulator.buildResults(totalPopulation);

        Map<UUID, BigDecimal> byCandidate = results.stream()
                .collect(Collectors.toMap(CandidateWeightedResult::candidateId, CandidateWeightedResult::weightedPercentage));

        assertThat(totalPopulation).isEqualTo(4_000);
        assertThat(byCandidate.get(candidate1.getId())).isEqualByComparingTo("37.50");
        assertThat(byCandidate.get(candidate2.getId())).isEqualByComparingTo("62.50");
    }

    private static void setId(Object entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}

