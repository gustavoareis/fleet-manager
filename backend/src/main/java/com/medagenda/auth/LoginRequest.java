package com.medagenda.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Schema(example = "paciente@medagenda.local") String email,
        @NotBlank @Schema(example = "Senha@123") String password) {
}
