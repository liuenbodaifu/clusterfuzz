package com.google.clusterfuzz.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for ClusterFuzz Web API.
 * Provides comprehensive API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clusterFuzzOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClusterFuzz Java API")
                        .description("REST API for ClusterFuzz fuzzing infrastructure management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ClusterFuzz Team")
                                .email("clusterfuzz@google.com")
                                .url("https://github.com/google/clusterfuzz"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://clusterfuzz.example.com")
                                .description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("HTTP Basic Authentication")));
    }
}