package com.medagenda.admin;

import com.medagenda.scheduling.Appointment;
import com.medagenda.scheduling.AppointmentStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One line of the reception list. Times are formatted here, in Java, because JSTL's formatDate
 * does not understand java.time types.
 */
public record ReceptionRow(
        Long id,
        String time,
        String patientName,
        String doctorName,
        String specialty,
        String insurance,
        AppointmentStatus status,
        String statusLabel,
        boolean canConfirmArrival) {

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

    public static ReceptionRow from(Appointment appointment, LocalDateTime now) {
        var patient = appointment.getPatient();
        var doctor = appointment.getDoctor();
        var insurance = patient.getHealthInsurance();
        return new ReceptionRow(
                appointment.getId(),
                HOUR.format(appointment.getScheduledAt()),
                patient.getName(),
                doctor.getName(),
                doctor.getSpecialty().getName(),
                insurance == null ? "Particular" : insurance.getName(),
                appointment.getStatus(),
                StatusLabels.of(appointment.getStatus()),
                appointment.canConfirmArrival(now));
    }
}
