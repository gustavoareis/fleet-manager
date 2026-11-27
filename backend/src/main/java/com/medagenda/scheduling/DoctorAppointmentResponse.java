package com.medagenda.scheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An appointment as the doctor's agenda needs it. {@code canRecordOutcome} is computed by the
 * server (status + "the time has arrived") so the frontend doesn't duplicate the rule.
 * {@code notes} is null unless the viewer is the doctor: reception must not read clinical notes.
 */
public record DoctorAppointmentResponse(
        Long id,
        LocalDateTime scheduledAt,
        AppointmentStatus status,
        PatientInfo patient,
        String notes,
        boolean canRecordOutcome) {

    public record PatientInfo(Long id, String name, LocalDate birthDate, String phone, String insurance) {
    }

    public static DoctorAppointmentResponse from(Appointment appointment, LocalDateTime now, boolean includeNotes) {
        var patient = appointment.getPatient();
        var insurance = patient.getHealthInsurance();
        return new DoctorAppointmentResponse(
                appointment.getId(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                new PatientInfo(patient.getId(), patient.getName(), patient.getBirthDate(), patient.getPhone(),
                        insurance == null ? null : insurance.getName()),
                includeNotes ? appointment.getNotes() : null,
                appointment.canRecordOutcome(now));
    }
}
