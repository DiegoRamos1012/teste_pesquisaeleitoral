package com.diegoramos.konatus.pesquisaeleitoral.service.ibge;

import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeMunicipalityResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeStateResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class IbgeApiClient {

    private final RestClient localityRestClient;
    private final RestClient sidraRestClient;

    public IbgeApiClient(
            RestClient.Builder restClientBuilder,
            @Value("${ibge.api.base-url:https://servicodados.ibge.gov.br/api/v1/localidades}") String localityBaseUrl,
            @Value("${ibge.sidra.base-url:https://servicodados.ibge.gov.br/api/v3}") String sidraBaseUrl
    ) {
        this.localityRestClient = restClientBuilder.baseUrl(localityBaseUrl).build();
        this.sidraRestClient = restClientBuilder.baseUrl(sidraBaseUrl).build();
    }

    public List<IbgeStateResponseDTO> getStates() {
        IbgeStateResponseDTO[] response = localityRestClient
                .get()
                .uri("/estados")
                .retrieve()
                .body(IbgeStateResponseDTO[].class);

        if (response == null) {
            throw new BusinessException("IBGE retornou resposta vazia para estados");
        }

        return Arrays.asList(response);
    }

    public List<IbgeMunicipalityResponseDTO> getMunicipalitiesByStateAcronym(String stateAcronym) {
        IbgeMunicipalityResponseDTO[] response = localityRestClient
                .get()
                .uri("/estados/{uf}/municipios", stateAcronym)
                .retrieve()
                .body(IbgeMunicipalityResponseDTO[].class);

        if (response == null) {
            throw new BusinessException("IBGE retornou resposta vazia para municipios de " + stateAcronym);
        }

        return Arrays.asList(response);
    }

    public Integer getMunicipalityPopulationByCode(Long municipalityCode) {
        if (municipalityCode == null) {
            return null;
        }

        JsonNode response = sidraRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/agregados/6579/periodos/-6/variaveis/9324")
                        .queryParam("localidades", "N6[" + municipalityCode + "]")
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.isArray() || response.isEmpty()) {
            return null;
        }

        JsonNode seriesNode = response.path(0).path("resultados").path(0).path("series").path(0).path("serie");
        if (!seriesNode.isObject()) {
            return null;
        }

        int latestYear = Integer.MIN_VALUE;
        Integer latestPopulation = null;

        for (Map.Entry<String, JsonNode> entry : iterable(seriesNode.fields())) {
            try {
                int year = Integer.parseInt(entry.getKey());
                String rawValue = entry.getValue().asText();
                Integer population = parsePopulation(rawValue);
                if (population != null && year > latestYear) {
                    latestYear = year;
                    latestPopulation = population;
                }
            } catch (NumberFormatException ignored) {
                // Ignora chaves nao numericas de periodo.
            }
        }

        return latestPopulation;
    }

    private Integer parsePopulation(String rawValue) {
        if (rawValue == null || rawValue.isBlank() || "-".equals(rawValue)) {
            return null;
        }

        String digitsOnly = rawValue.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
        return () -> iterator;
    }
}


