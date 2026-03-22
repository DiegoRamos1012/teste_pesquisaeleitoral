package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.ResearchLine;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
class PollCsvParser {

    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");
    private static final DateTimeFormatter DATE_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    List<ResearchLine> parse(MultipartFile archive) {
        if (archive.isEmpty()) {
            throw new BusinessException("Arquivo CSV vazio");
        }

        try (Reader reader = new InputStreamReader(archive.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                throw new BusinessException("Arquivo CSV sem conteúdo");
            }

            Map<String, Integer> header = createHeaderMap(rows.getFirst());
            List<ResearchLine> result = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int lineNumber = i + 1;

                if (isEmptyRow(row)) {
                    continue;
                }

                ResearchLine line = new ResearchLine(
                        getOptionalValue(row, header, aliases("poll_id", "id_pesquisa", "id da pesquisa"), "PESQUISA-IMPORTADA"),
                        parsePollDate(getOptionalValue(row, header, aliases("poll_date", "data_pesquisa", "data da pesquisa"), LocalDate.now().toString()), lineNumber),
                        getRequiredValue(getByHeader(row, header, aliases("estado", "uf", "state"), lineNumber), "estado", lineNumber),
                        getRequiredValue(getByHeader(row, header, aliases("municipio", "municipality"), lineNumber), "municipio", lineNumber),
                        parseUuid(getByHeader(row, header, aliases("candidate_id", "id_candidato", "candidato_id"), lineNumber), lineNumber),
                        parsePercentual(getByHeader(row, header, aliases("percentual", "percentage", "intencao"), lineNumber), lineNumber)
                );

                result.add(line);
            }

            return result;
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

    private String getOptionalValue(String[] row, Map<String, Integer> header, List<String> aliases, String defaultValue) {
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
            if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(MAX_PERCENTAGE) > 0) {
                throw new BusinessException("Linha " + lineNumber + " invalida: percentual deve estar entre 0 e 100");
            }
            return percentual;
        } catch (NumberFormatException e) {
            throw new BusinessException("Linha " + lineNumber + " invalida: percentual numerico esperado");
        }
    }
}


