package com.medagenda.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.hibernate.validator.constraints.br.CPF;

/**
 * The API takes CPF and phone as digits only; the frontend applies and strips the masks.
 * {@code @CPF} (Hibernate Validator) checks the two verification digits.
 */
public record PatientCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "deve conter 11 dígitos") @CPF
        @Schema(example = "52998224725") String cpf,
        @NotBlank @Pattern(regexp = PatientUpdateRequest.PHONE_PATTERN, message = "deve conter DDD + número (10 ou 11 dígitos)")
        @Schema(example = "11987654321") String phone,
        @NotNull @Past LocalDate birthDate,
        @Schema(description = "Vazio para atendimento particular") Long healthInsuranceId,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
