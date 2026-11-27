package com.medagenda.scheduling;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * "Is it in the future?" is checked in the service with the clinic Clock, not with @Future,
 * which would use the server's default time zone.
 */
public record AppointmentRequest(
        @NotNull Long doctorId,
        @NotNull @Schema(example = "2026-12-07T09:00:00", description = "Horário local da clínica")
        LocalDateTime scheduledAt,
        @Schema(description = "Obrigatório quando a recepção agenda; ignorado para pacientes (usa o paciente logado)")
        Long patientId) {
}
