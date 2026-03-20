package com.diegoramos.konatus.pesquisaeleitoral.service.ibge;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeMunicipalityResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeStateResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeSyncResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IbgeService {

    private static final int PROGRESS_BAR_WIDTH = 24;

    private final IbgeApiClient ibgeApiClient;
    private final StateRepository stateRepository;
    private final MunicipalityRepository municipalityRepository;

    @Transactional
    public IbgeSyncResultDTO syncStatesAndMunicipalities(boolean force) {
        long startedAt = System.nanoTime();
        String runId = UUID.randomUUID().toString().substring(0, 8);

        if (force) {
            ibgeApiClient.clearPopulationCache();
        }

        int statesCreated = 0;
        int statesUpdated = 0;
        int municipalitiesCreated = 0;
        int municipalitiesUpdated = 0;

        List<IbgeStateResponseDTO> ibgeStates = ibgeApiClient.getStates();
        int totalStates = ibgeStates.size();

        log.info("[{}] Iniciando sync IBGE: totalUfs={}, force={}", runId, totalStates, force);

        Map<String, State> statesByAcronym = new HashMap<>();
        for (State state : stateRepository.findAll()) {
            statesByAcronym.put(state.getStateAcronym().toUpperCase(Locale.ROOT), state);
        }

        for (int stateIndex = 0; stateIndex < ibgeStates.size(); stateIndex++) {
            long stateStartedAt = System.nanoTime();
            IbgeStateResponseDTO ibgeState = ibgeStates.get(stateIndex);

            if (ibgeState.sigla() == null || ibgeState.nome() == null) {
                continue;
            }

            String acronym = ibgeState.sigla().trim().toUpperCase(Locale.ROOT);
            String stateName = ibgeState.nome().trim();

            State state = statesByAcronym.get(acronym);
            if (state != null) {
                if (!state.getName().equals(stateName)) {
                    state.updateName(stateName);
                    statesUpdated++;
                }
            } else {
                state = stateRepository.save(State.create(stateName, acronym));
                statesByAcronym.put(acronym, state);
                statesCreated++;
            }

            Map<String, Municipality> municipalitiesByName = new HashMap<>();
            for (Municipality municipality : municipalityRepository.findAllByState(state)) {
                municipalitiesByName.put(normalizeName(municipality.getName()), municipality);
            }

            List<Municipality> newMunicipalities = new ArrayList<>();
            int stateMunicipalitiesUpdated = 0;
            List<IbgeMunicipalityResponseDTO> ibgeMunicipalities = ibgeApiClient.getMunicipalitiesByStateAcronym(acronym);

            for (IbgeMunicipalityResponseDTO ibgeMunicipality : ibgeMunicipalities) {
                if (ibgeMunicipality.nome() == null) {
                    continue;
                }
                String municipalityName = ibgeMunicipality.nome().trim();
                Integer population = ibgeApiClient.getMunicipalityPopulationByCode(ibgeMunicipality.id());
                if (population == null) {
                    population = 0;
                }

                Municipality municipality = municipalitiesByName.get(normalizeName(municipalityName));
                if (municipality != null) {
                    if (municipality.getPopulation() != population) {
                        municipality.updatePopulation(population);
                        stateMunicipalitiesUpdated++;
                    }
                } else {
                    Municipality created = Municipality.create(municipalityName, population, state);
                    newMunicipalities.add(created);
                    municipalitiesByName.put(normalizeName(municipalityName), created);
                }
            }

            if (!newMunicipalities.isEmpty()) {
                municipalityRepository.saveAll(newMunicipalities);
                municipalitiesCreated += newMunicipalities.size();
            }

            municipalitiesUpdated += stateMunicipalitiesUpdated;
            int statesProcessed = stateIndex + 1;
            int percentage = totalStates == 0 ? 100 : (int) Math.round((statesProcessed * 100.0) / totalStates);
            long stateElapsedMillis = nanosToMillis(System.nanoTime() - stateStartedAt);
            long elapsedMillis = nanosToMillis(System.nanoTime() - startedAt);
            long avgPerStateMillis = statesProcessed == 0 ? 0 : elapsedMillis / statesProcessed;
            long etaMillis = Math.max(0, avgPerStateMillis * (totalStates - statesProcessed));
            log.info(
                    "[{}] IBGE sync {} {}% ({}/{}) UF={} municipios={} novos={} atualizados={} tempoUF={} ETA={}",
                    runId,
                    buildProgressBar(statesProcessed, totalStates),
                    String.format("%3d", percentage),
                    statesProcessed,
                    totalStates,
                    acronym,
                    ibgeMunicipalities.size(),
                    newMunicipalities.size(),
                    stateMunicipalitiesUpdated,
                    stateElapsedMillis,
                    formatDuration(etaMillis)
            );
        }

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        log.info(
                "[{}] IBGE sync concluido em {} ({} ms) (estados criados={}, estados atualizados={}, municipios criados={}, municipios atualizados={}, force={})",
                runId,
                formatDuration(elapsedMillis),
                elapsedMillis,
                statesCreated,
                statesUpdated,
                municipalitiesCreated,
                municipalitiesUpdated,
                force
        );

        return new IbgeSyncResultDTO(statesCreated, statesUpdated, municipalitiesCreated, force);
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }

    private String formatDuration(long millis) {
        return String.format(Locale.ROOT, "%.2fs", millis / 1000.0);
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private String buildProgressBar(int current, int total) {
        int safeTotal = Math.max(total, 1);
        int filled = Math.max(0, Math.min(PROGRESS_BAR_WIDTH, (int) Math.round((current * 1.0 / safeTotal) * PROGRESS_BAR_WIDTH)));
        return "[" + "#".repeat(filled) + "-".repeat(PROGRESS_BAR_WIDTH - filled) + "]";
    }
}

