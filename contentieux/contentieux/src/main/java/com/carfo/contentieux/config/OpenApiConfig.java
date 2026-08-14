package com.carfo.contentieux.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI carfoContentieuxOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Contentieux CARFO")
                        .description("API de gestion des dossiers contentieux de la CARFO.")
                        .version("1.0.0")
                        .contact(new Contact().name("Service Contentieux et Juridique CARFO")));
    }
}
