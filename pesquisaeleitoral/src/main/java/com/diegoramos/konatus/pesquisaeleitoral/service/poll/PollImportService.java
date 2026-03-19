package com.diegoramos.konatus.pesquisaeleitoral.service.poll;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Poll;
import com.diegoramos.konatus.pesquisaeleitoral.domain.PollResult;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import com.diegoramos.konatus.pesquisaeleitoral.repository.CandidateRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.PollRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.PollResultRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.StateRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PollImportService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final DateTimeFormatter DATE_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StateRepository stateRepository;
    private final MunicipalityRepository municipalityRepository;
    private final CandidateRepository candidateRepository;
    private final PollRepository pollRepository;
    private final PollResultRepository pollResultRepository;

    @Transactional
    public PollImportResult researchImport(MultipartFile archive) {
        if (archive.isEmpty()) {
            throw new BusinessException("Arquivo CSV vazio");
        }

        List<ResearchLine> linhas = readCSV(archive);
        if (linhas.isEmpty()) {
            throw new BusinessException("Arquivo CSV sem linhas de dados");
        }

        String pollId = linhas.getFirst().pollId();
        LocalDate pollDate = linhas.getFirst().pollDate();

        validateSamePollMetadata(linhas, pollId, pollDate);

        Poll poll = pollRepository.findByNameAndPollDate(pollId, pollDate)
                .orElseGet(() -> pollRepository.save(Poll.create(pollId, pollDate)));

        Map<UUID, BigDecimal> weightedCandidateVotes = new HashMap<>();
        Map<UUID, String> candidateNames = new HashMap<>();
        Map<String, Integer> uniqueMunicipalityPopulation = new HashMap<>();

        for (ResearchLine linha : linhas) {
            State state = resolveState(linha.state());
            Municipality municipality = resolveMunicipality(linha.municipality(), state);
            Candidate candidate = candidateRepository.findById(linha.candidateId())
                    .orElseThrow(() -> new BusinessException("Candidato nao encontrado: " + linha.candidateId()));
            candidateNames.putIfAbsent(candidate.getId(), candidate.getName());

            pollResultRepository.save(PollResult.create(poll, municipality, candidate, linha.percentual()));

            String municipalityKey = state.getId() + ":" + municipality.getId();
            uniqueMunicipalityPopulation.putIfAbsent(municipalityKey, municipality.getPopulation());

            BigDecimal weightedValue = linha.percentual()
                    .multiply(BigDecimal.valueOf(municipality.getPopulation()))
                    .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);

            weightedCandidateVotes.merge(candidate.getId(), weightedValue, BigDecimal::add);
        }

        long totalPopulation = uniqueMunicipalityPopulation.values().stream().mapToLong(Integer::longValue).sum();
        if (totalPopulation <= 0) {
            throw new BusinessException("Nao foi possivel calcular ponderacao: populacao dos municipios esta zerada");
        }

        List<CandidateWeightedResult> weightedResults = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : weightedCandidateVotes.entrySet()) {
            BigDecimal result = entry.getValue()
                    .divide(BigDecimal.valueOf(totalPopulation), 8, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
            weightedResults.add(new CandidateWeightedResult(
                    entry.getKey(),
                    candidateNames.getOrDefault(entry.getKey(), "Candidato sem nome"),
                    result
            ));
        }

        weightedResults.sort(Comparator.comparing(CandidateWeightedResult::weightedPercentage).reversed());

        return new PollImportResult(pollId, pollDate, totalPopulation, weightedResults);
    }

    private List<ResearchLine> readCSV(MultipartFile archive) {
        try (Reader reader = new InputStreamReader(archive.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                throw new BusinessException("Arquivo CSV sem conteúdo");
            }

            Map<String, Integer> header = createHeaderMap(rows.getFirst());

            List<ResearchLine> resultado = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int lineNumber = i + 1;
                if (isEmptyRow(row)) {
                    continue;
                }

                ResearchLine linha = new ResearchLine(
                        getOptionalValue(row, header, aliases("poll_id", "id_pesquisa", "id da pesquisa"), "PESQUISA-IMPORTADA", lineNumber),
                        parsePollDate(getOptionalValue(row, header, aliases("poll_date", "data_pesquisa", "data da pesquisa"), LocalDate.now().toString(), lineNumber), lineNumber),
                        getRequiredValue(getByHeader(row, header, aliases("estado", "uf", "state"), lineNumber), "estado", lineNumber),
                        getRequiredValue(getByHeader(row, header, aliases("municipio", "municipio", "municipality"), lineNumber), "municipio", lineNumber),
                        parseUuid(getByHeader(row, header, aliases("candidate_id", "id_candidato", "candidato_id"), lineNumber), lineNumber),
                        parsePercentual(getByHeader(row, header, aliases("percentual", "percentage", "intencao"), lineNumber), lineNumber)
                );

                resultado.add(linha);
            }

            return resultado;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erro ao ler arquivo CSV: " + e.getMessage());
        }
    }

    private Map<String, Integer> createHeaderMap(String[] headerRow) {
        Map<String, Integer> header = new HashMap<>();
        for (int i = 0; i < headerRow.length; i++) {
            header.put(normalizeHeader(headerRow[i]), i);
        }
        return header;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private List<String> aliases(String... values) {
        return List.of(values);
    }

    private String getByHeader(String[] row, Map<String, Integer> header, List<String> aliases, int lineNumber) {
        for (String alias : aliases) {
            Integer index = header.get(normalizeHeader(alias));
            if (index != null) {
                if (index >= row.length) {
                    throw new BusinessException("Linha " + lineNumber + " invalida: coluna '" + alias + "' ausente");
                }
                return row[index];
            }
        }
        throw new BusinessException("Cabecalho invalido: coluna obrigatoria nao encontrada " + aliases);
    }

    private String getOptionalValue(String[] row, Map<String, Integer> header, List<String> aliases, String defaultValue, int lineNumber) {
        for (String alias : aliases) {
            Integer index = header.get(normalizeHeader(alias));
            if (index != null && index < row.length) {
                String value = row[index];
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }
        return defaultValue;
    }

    private boolean isEmptyRow(String[] row) {
        for (String value : row) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parsePollDate(String value, int lineNumber) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(value, DATE_BR);
            } catch (DateTimeParseException e) {
                throw new BusinessException("Linha " + lineNumber + " invalida: data da pesquisa em formato invalido");
            }
        }
    }

    private void validateSamePollMetadata(List<ResearchLine> linhas, String pollId, LocalDate pollDate) {
        for (ResearchLine linha : linhas) {
            if (!pollId.equals(linha.pollId()) || !pollDate.equals(linha.pollDate())) {
                throw new BusinessException("Arquivo deve conter apenas uma pesquisa por importacao");
            }
        }
    }

    private State resolveState(String stateValue) {
        return stateRepository.findByStateAcronymIgnoreCase(stateValue)
                .or(() -> stateRepository.findByNameIgnoreCase(stateValue))
                .orElseThrow(() -> new BusinessException("Estado nao encontrado na base: " + stateValue));
    }

    private Municipality resolveMunicipality(String municipalityName, State state) {
        Optional<Municipality> municipality = municipalityRepository.findByNameIgnoreCaseAndState(municipalityName, state);
        if (municipality.isEmpty()) {
            throw new BusinessException("Municipio nao encontrado para o estado " + state.getStateAcronym() + ": " + municipalityName);
        }
        return municipality.get();
    }

    private String getRequiredValue(String value, String fieldName, int lineNumber) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("Linha " + lineNumber + " invalida: campo '" + fieldName + "' obrigatorio");
        }
        return value.trim();
    }

    private UUID parseUuid(String value, int lineNumber) {
        String normalized = getRequiredValue(value, "candidate_id", lineNumber);
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Linha " + lineNumber + " invalida: candidate_id deve ser UUID");
        }
    }

    private BigDecimal parsePercentual(String value, int lineNumber) {
        String normalized = getRequiredValue(value, "percentual", lineNumber);
        try {
            BigDecimal percentual = new BigDecimal(normalized);
            if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("Linha " + lineNumber + " invalida: percentual deve estar entre 0 e 100");
            }
            return percentual;
        } catch (NumberFormatException e) {
            throw new BusinessException("Linha " + lineNumber + " invalida: percentual numerico esperado");
        }
    }
}