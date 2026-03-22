package com.diegoramos.konatus.pesquisaeleitoral.service.ibge;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeMunicipalityResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeStateResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeSyncResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.StateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IbgeServiceTest {

    @Mock
    private IbgeApiClient ibgeApiClient;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @InjectMocks
    private IbgeService ibgeService;

    @Test
    void shouldNotPersistWhenIbgeDataIsUnchanged() {
        State state = State.create("Sao Paulo", "SP");
        Municipality municipality = Municipality.create("Campinas", 1_000, state);

        when(stateRepository.findAll()).thenReturn(List.of(state));
        when(municipalityRepository.findAllByState(state)).thenReturn(List.of(municipality));
        when(ibgeApiClient.getStates()).thenReturn(List.of(new IbgeStateResponseDTO(35L, "SP", "Sao Paulo")));
        when(ibgeApiClient.getMunicipalitiesByStateAcronym("SP"))
                .thenReturn(List.of(new IbgeMunicipalityResponseDTO(3509502L, "Campinas")));
        when(ibgeApiClient.getMunicipalityPopulationByCode(3509502L)).thenReturn(1_000);

        IbgeSyncResultDTO result = ibgeService.syncStatesAndMunicipalities(false);

        assertThat(result.statesCreated()).isZero();
        assertThat(result.statesUpdated()).isZero();
        assertThat(result.municipalitiesCreated()).isZero();
        assertThat(result.forced()).isFalse();

        verify(stateRepository, never()).save(any(State.class));
        verify(municipalityRepository, never()).saveAll(any());
    }

    @Test
    void shouldCreateStateAndMunicipalityWhenNotPresentLocally() {
        State persistedState = State.create("Acre", "AC");

        when(stateRepository.findAll()).thenReturn(List.of());
        when(stateRepository.save(any(State.class))).thenReturn(persistedState);
        when(municipalityRepository.findAllByState(persistedState)).thenReturn(List.of());
        when(ibgeApiClient.getStates()).thenReturn(List.of(new IbgeStateResponseDTO(12L, "AC", "Acre")));
        when(ibgeApiClient.getMunicipalitiesByStateAcronym("AC"))
                .thenReturn(List.of(new IbgeMunicipalityResponseDTO(1200401L, "Rio Branco")));
        when(ibgeApiClient.getMunicipalityPopulationByCode(1200401L)).thenReturn(364_756);

        IbgeSyncResultDTO result = ibgeService.syncStatesAndMunicipalities(false);

        assertThat(result.statesCreated()).isEqualTo(1);
        assertThat(result.statesUpdated()).isZero();
        assertThat(result.municipalitiesCreated()).isEqualTo(1);

        verify(stateRepository).save(any(State.class));
        verify(municipalityRepository).saveAll(any());
    }
}

