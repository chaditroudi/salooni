package com.glowzi.identity.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI glowziIdentityOpenAPI() {
        final String securitySchemeName = "Bearer JWT";

        return new OpenAPI()
                .info(new Info()
                        .title("Glowzi Identity Service API")
                        .description("Authentication and user management API for Glowzi salon booking platform. "
                                + "Use the Authorize button to set your Keycloak access token.")
                        .version("1.0.0")
                        .contact(new Contact().name("Glowzi Team")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your Keycloak access token here")));
    }
}
