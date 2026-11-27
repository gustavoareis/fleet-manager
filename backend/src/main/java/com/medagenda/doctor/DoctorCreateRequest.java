package com.medagenda.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Creates the doctor profile together with the login account. */
public record DoctorCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = DoctorUpdateRequest.CRM_PATTERN, message = "deve seguir o formato 123456/SP")
        String crm,
        @NotNull Long specialtyId,
        @NotBlank @Email @Size(max = 160) String email,
        // BCrypt only uses the first 72 bytes of a password
        @NotBlank @Size(min = 8, max = 72) String password) {
}
