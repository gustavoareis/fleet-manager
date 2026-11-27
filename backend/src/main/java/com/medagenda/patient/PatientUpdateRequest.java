package com.medagenda.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PatientUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = PHONE_PATTERN, message = "deve conter DDD + número (10 ou 11 dígitos)")
        @Schema(example = "11987654321") String phone,
        @NotNull @Past LocalDate birthDate,
        @Schema(description = "Vazio para atendimento particular") Long healthInsuranceId) {

    static final String PHONE_PATTERN = "\\d{10,11}";
}
