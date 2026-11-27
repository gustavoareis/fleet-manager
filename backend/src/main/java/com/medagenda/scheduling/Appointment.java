package com.medagenda.scheduling;

import com.medagenda.common.error.BusinessException;
import com.medagenda.doctor.Doctor;
import com.medagenda.patient.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * "Consulta". Rules that depend only on the appointment itself (state transitions, the
 * cancellation deadline) live here; rules that need other data (availability, conflicts)
 * live in the booking service.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    /** States that still occupy the time slot. */
    public static final Set<AppointmentStatus> ACTIVE_STATUSES =
            Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Appointment() {
        // required by JPA
    }

    public Appointment(Patient patient, Doctor doctor, LocalDateTime scheduledAt, LocalDateTime createdAt) {
        this.patient = patient;
        this.doctor = doctor;
        this.scheduledAt = scheduledAt;
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(status);
    }

    /** Active and at least {@code minNotice} before the appointment time. */
    public boolean canBeCancelled(LocalDateTime now, Duration minNotice) {
        return isActive() && !now.plus(minNotice).isAfter(scheduledAt);
    }

    public void cancel(LocalDateTime now, Duration minNotice) {
        if (!isActive()) {
            throw new BusinessException("Só é possível cancelar consultas agendadas ou confirmadas.");
        }
        if (!canBeCancelled(now, minNotice)) {
            throw new BusinessException("O cancelamento só é permitido com no mínimo %d horas de antecedência."
                    .formatted(minNotice.toHours()));
        }
        this.status = AppointmentStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
