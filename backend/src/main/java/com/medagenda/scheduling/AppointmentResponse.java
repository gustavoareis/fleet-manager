package com.medagenda.scheduling;

import java.time.LocalDateTime;

/**
 * {@code cancellable} is computed by the server (status + 24h rule), so the frontend shows the
 * cancel button without duplicating the rule. Clinical notes are intentionally not exposed here.
 */
public record AppointmentResponse(
        Long id,
        LocalDateTime scheduledAt,
        AppointmentStatus status,
        DoctorSummary doctor,
        PatientSummary patient,
        boolean cancellable) {

    public record DoctorSummary(Long id, String name, String specialty) {
    }

    public record PatientSummary(Long id, String name) {
    }

    public static AppointmentResponse from(Appointment appointment, boolean cancellable) {
        var doctor = appointment.getDoctor();
        var patient = appointment.getPatient();
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                new DoctorSummary(doctor.getId(), doctor.getName(), doctor.getSpecialty().getName()),
                new PatientSummary(patient.getId(), patient.getName()),
                cancellable);
    }
}
