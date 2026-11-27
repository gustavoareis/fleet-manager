package com.medagenda.doctor;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes) {

    public static AvailabilityResponse from(DoctorAvailability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getSlotDurationMinutes());
    }
}
