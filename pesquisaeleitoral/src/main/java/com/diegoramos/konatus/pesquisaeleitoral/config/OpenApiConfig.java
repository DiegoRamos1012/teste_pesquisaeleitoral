package com.diegoramos.konatus.pesquisaeleitoral.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pesquisaEleitoralOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Pesquisa Eleitoral API - Teste Konatus")
                .description("API para sincronização IBGE, importação de pesquisas e cálculo de intencão de votos")
                .version("v1")
                .contact(new Contact().name("Diego Ramos dos Santos")));
    }
}

