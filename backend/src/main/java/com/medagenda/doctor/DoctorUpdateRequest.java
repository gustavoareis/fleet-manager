package com.medagenda.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Profile data only; e-mail and password belong to the account and change through the auth endpoints. */
public record DoctorUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = CRM_PATTERN, message = "deve seguir o formato 123456/SP") String crm,
        @NotNull Long specialtyId) {

    static final String CRM_PATTERN = "^\\s*\\d{4,7}/[A-Za-z]{2}\\s*$";
}
