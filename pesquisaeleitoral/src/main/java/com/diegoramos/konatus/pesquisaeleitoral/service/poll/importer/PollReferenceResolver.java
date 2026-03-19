package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import com.diegoramos.konatus.pesquisaeleitoral.repository.CandidateRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class PollReferenceResolver {

    private final StateRepository stateRepository;
    private final MunicipalityRepository municipalityRepository;
    private final CandidateRepository candidateRepository;

    State resolveState(String stateValue) {
        return stateRepository.findByStateAcronymIgnoreCase(stateValue)
                .or(() -> stateRepository.findByNameIgnoreCase(stateValue))
                .orElseThrow(() -> new BusinessException("Estado nao encontrado na base: " + stateValue));
    }

    Municipality resolveMunicipality(String municipalityName, State state) {
        return municipalityRepository.findByNameIgnoreCaseAndState(municipalityName, state)
                .orElseThrow(() -> new BusinessException(
                        "Municipio nao encontrado para o estado " + state.getStateAcronym() + ": " + municipalityName
                ));
    }

    Candidate resolveCandidate(UUID candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException("Candidato nao encontrado: " + candidateId));
    }
}


