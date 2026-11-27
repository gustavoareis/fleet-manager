package com.medagenda.doctor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull @Schema(type = "string", example = "08:00") LocalTime startTime,
        @NotNull @Schema(type = "string", example = "12:00") LocalTime endTime,
        @NotNull @Min(5) @Max(240) Integer slotDurationMinutes) {
}
