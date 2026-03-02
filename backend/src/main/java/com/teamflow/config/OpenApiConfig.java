package com.teamflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * @SecurityScheme: defines a reusable "bearerAuth" scheme.
 * Controllers reference it via @SecurityRequirement(name = "bearerAuth").
 * This makes the Swagger UI show an "Authorize" button where you can
 * paste a JWT token and test protected endpoints directly.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TeamFlow API",
                version = "1.0",
                description = "Project and Task Management SaaS — REST API documentation"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
