package com.medagenda.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "MedAgenda API",
        version = "v1",
        description = "Agendamento de consultas para clínicas: pacientes, médicos, especialidades, convênios e agenda."))
public class OpenApiConfig {
}
