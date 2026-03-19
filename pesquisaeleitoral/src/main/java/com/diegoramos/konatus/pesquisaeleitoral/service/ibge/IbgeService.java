package com.diegoramos.konatus.pesquisaeleitoral.service.ibge;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeMunicipalityResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeStateResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeSyncResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IbgeService {

    private final IbgeApiClient ibgeApiClient;
    private final StateRepository stateRepository;
    private final MunicipalityRepository municipalityRepository;

    @Transactional
    public IbgeSyncResultDTO syncStatesAndMunicipalities(boolean force) {
        int statesCreated = 0;
        int statesUpdated = 0;
        int municipalitiesCreated = 0;

        for (IbgeStateResponseDTO ibgeState : ibgeApiClient.getStates()) {
            if (ibgeState.sigla() == null || ibgeState.nome() == null) {
                continue;
            }

            String acronym = ibgeState.sigla().trim().toUpperCase(Locale.ROOT);
            String stateName = ibgeState.nome().trim();

            Optional<State> existingState = stateRepository.findByStateAcronymIgnoreCase(acronym);
            State state;
            if (existingState.isPresent()) {
                state = existingState.get();
                if (!state.getName().equals(stateName)) {
                    state.updateName(stateName);
                    statesUpdated++;
                }
            } else {
                state = stateRepository.save(State.create(stateName, acronym));
                statesCreated++;
            }

            for (IbgeMunicipalityResponseDTO ibgeMunicipality : ibgeApiClient.getMunicipalitiesByStateAcronym(acronym)) {
                if (ibgeMunicipality.nome() == null) {
                    continue;
                }
                String municipalityName = ibgeMunicipality.nome().trim();
                Integer population = ibgeApiClient.getMunicipalityPopulationByCode(ibgeMunicipality.id());
                if (population == null) {
                    population = 0;
                }

                Optional<Municipality> municipalityOptional = municipalityRepository.findByNameIgnoreCaseAndState(municipalityName, state);
                if (municipalityOptional.isPresent()) {
                    Municipality municipality = municipalityOptional.get();
                    if (municipality.getPopulation() != population) {
                        municipality.updatePopulation(population);
                    }
                } else {
                    municipalityRepository.save(Municipality.create(municipalityName, population, state));
                    municipalitiesCreated++;
                }
            }
        }

        return new IbgeSyncResultDTO(statesCreated, statesUpdated, municipalitiesCreated, force);
    }
}

