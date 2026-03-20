package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Poll;
import com.diegoramos.konatus.pesquisaeleitoral.domain.PollResult;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.PollRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.PollResultRepository;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.CandidateWeightedResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollGroupBreakdown;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollImportResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.ResearchLine;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.weighting.PollWeightAccumulator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PollImportService {

    private final PollCsvParser pollCsvParser;
    private final PollReferenceResolver pollReferenceResolver;
    private final MunicipalityRepository municipalityRepository;
    private final PollRepository pollRepository;
    private final PollResultRepository pollResultRepository;

    @Transactional
    public PollImportResult researchImport(MultipartFile archive) {
        List<ResearchLine> linhas = pollCsvParser.parse(archive);
        if (linhas.isEmpty()) {
            throw new BusinessException("Arquivo CSV sem linhas de dados");
        }

        String pollId = linhas.getFirst().pollId();
        LocalDate pollDate = linhas.getFirst().pollDate();

        validateSamePollMetadata(linhas, pollId, pollDate);

        Poll poll = pollRepository.findByNameAndPollDate(pollId, pollDate)
                .orElseGet(() -> pollRepository.save(Poll.create(pollId, pollDate)));

        PollWeightAccumulator weightAccumulator = new PollWeightAccumulator();
        Map<UUID, Candidate> candidatesById = new HashMap<>();
        Map<UUID, Boolean> populatedStates = new HashMap<>();

        for (ResearchLine linha : linhas) {
            processResearchLine(linha, poll, weightAccumulator, candidatesById, populatedStates);
        }

        long totalPopulation = weightAccumulator.totalPopulation();
        if (totalPopulation <= 0) {
            throw new BusinessException("Nao foi possivel calcular ponderacao: populacao dos municipios esta zerada");
        }

        List<CandidateWeightedResult> weightedResults = weightAccumulator.buildResults(totalPopulation);
        List<PollGroupBreakdown> groupBreakdown = weightAccumulator.buildGroupBreakdown();

        weightedResults.sort(Comparator.comparing(CandidateWeightedResult::weightedPercentage).reversed());

        return new PollImportResult(pollId, pollDate, totalPopulation, weightedResults, groupBreakdown);
    }

    private void processResearchLine(
            ResearchLine line,
            Poll poll,
            PollWeightAccumulator weightAccumulator,
            Map<UUID, Candidate> candidatesById,
            Map<UUID, Boolean> populatedStates
    ) {
        State state = pollReferenceResolver.resolveState(line.state());
        Municipality municipality = pollReferenceResolver.resolveMunicipality(line.municipality(), state);
        Candidate candidate = resolveCandidateCached(line.candidateId(), candidatesById);

        if (!populatedStates.containsKey(state.getId())) {
            weightAccumulator.registerStateMunicipalities(state, municipalityRepository.findAllByState(state));
            populatedStates.put(state.getId(), true);
        }

        pollResultRepository.save(PollResult.create(poll, municipality, candidate, line.percentual()));
        weightAccumulator.addSample(state, municipality, candidate, line.percentual());
    }

    private Candidate resolveCandidateCached(UUID candidateId, Map<UUID, Candidate> candidatesById) {
        return candidatesById.computeIfAbsent(
                candidateId,
                pollReferenceResolver::resolveCandidate
        );
    }

    private void validateSamePollMetadata(List<ResearchLine> linhas, String pollId, LocalDate pollDate) {
        for (ResearchLine linha : linhas) {
            if (!pollId.equals(linha.pollId()) || !pollDate.equals(linha.pollDate())) {
                throw new BusinessException("Arquivo deve conter apenas uma pesquisa por importacao");
            }
        }
    }
}
