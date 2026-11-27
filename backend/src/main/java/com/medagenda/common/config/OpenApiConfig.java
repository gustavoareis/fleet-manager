package com.medagenda.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** The "Authorize" button in Swagger UI takes the token returned by POST /api/auth/login. */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MedAgenda API",
                version = "v1",
                description = "Agendamento de consultas para clínicas: pacientes, médicos, especialidades, "
                        + "convênios e agenda. Usuários de teste (perfil dev): paciente@, medico@, recepcao@ "
                        + "e admin@medagenda.local, senha Senha@123."),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(name = OpenApiConfig.BEARER_AUTH, type = SecuritySchemeType.HTTP, scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    static final String BEARER_AUTH = "bearerAuth";
}
