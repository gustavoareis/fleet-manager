package com.medagenda.scheduling;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorId(Long doctorId);

    boolean existsByPatientId(Long patientId);

    boolean existsByIdAndPatientUserId(Long id, Long userId);

    /** Slot taken by the doctor (cancelled appointments free the slot). */
    boolean existsByDoctorIdAndScheduledAtAndStatusIn(
            Long doctorId, LocalDateTime scheduledAt, Collection<AppointmentStatus> statuses);

    /** Patient already busy at that exact time. */
    boolean existsByPatientIdAndScheduledAtAndStatusIn(
            Long patientId, LocalDateTime scheduledAt, Collection<AppointmentStatus> statuses);

    /** Times already booked for the doctor in [from, to): one query for the whole day. */
    @Query("""
            select a.scheduledAt from Appointment a
            where a.doctor.id = :doctorId
              and a.scheduledAt >= :from and a.scheduledAt < :to
              and a.status in :statuses
            """)
    List<LocalDateTime> findBookedTimes(Long doctorId, LocalDateTime from, LocalDateTime to,
                                        Collection<AppointmentStatus> statuses);

    @EntityGraph(attributePaths = {"doctor", "doctor.specialty", "patient"})
    @Query("""
            select a from Appointment a
            where a.patient.id = :patientId
              and a.scheduledAt >= :now
              and a.status in :activeStatuses
            """)
    Page<Appointment> findUpcomingByPatient(Long patientId, LocalDateTime now,
                                           Collection<AppointmentStatus> activeStatuses, Pageable pageable);

    /** Everything that is not upcoming: past dates, or cancelled/completed/no-show at any date. */
    @EntityGraph(attributePaths = {"doctor", "doctor.specialty", "patient"})
    @Query("""
            select a from Appointment a
            where a.patient.id = :patientId
              and (a.scheduledAt < :now or a.status not in :activeStatuses)
            """)
    Page<Appointment> findHistoryByPatient(Long patientId, LocalDateTime now,
                                          Collection<AppointmentStatus> activeStatuses, Pageable pageable);
}
