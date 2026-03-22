package com.diegoramos.konatus.pesquisaeleitoral.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOriginPatternsRaw;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOriginPatterns = Arrays.stream(allowedOriginPatternsRaw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
