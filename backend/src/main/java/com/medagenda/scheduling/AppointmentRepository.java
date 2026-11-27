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

    boolean existsByIdAndDoctorUserId(Long id, Long userId);

    /** One doctor's agenda in [from, to), soonest first. The graph loads patient data in one query. */
    @EntityGraph(attributePaths = {"patient", "patient.healthInsurance", "doctor"})
    @Query("""
            select a from Appointment a
            where a.doctor.id = :doctorId
              and a.scheduledAt >= :from and a.scheduledAt < :to
            order by a.scheduledAt
            """)
    List<Appointment> findAgenda(Long doctorId, LocalDateTime from, LocalDateTime to);

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

    /** Reception list: every doctor's appointments in [from, to); doctorId = null means all doctors. */
    @EntityGraph(attributePaths = {"patient", "patient.healthInsurance", "doctor", "doctor.specialty"})
    @Query(value = """
            select a from Appointment a
            where a.scheduledAt >= :from and a.scheduledAt < :to
              and (:doctorId is null or a.doctor.id = :doctorId)
            """,
            countQuery = """
            select count(a) from Appointment a
            where a.scheduledAt >= :from and a.scheduledAt < :to
              and (:doctorId is null or a.doctor.id = :doctorId)
            """)
    Page<Appointment> findForReception(LocalDateTime from, LocalDateTime to, Long doctorId, Pageable pageable);

    /** Appointments per status in [from, to): one grouped query instead of one count per status. */
    @Query("""
            select a.status, count(a) from Appointment a
            where a.scheduledAt >= :from and a.scheduledAt < :to
            group by a.status
            """)
    List<Object[]> countByStatus(LocalDateTime from, LocalDateTime to);

    /** One row per doctor with the outcome counters, for the report. */
    @Query("""
            select new com.medagenda.admin.report.DoctorReportRow(
                d.name, s.name,
                count(a),
                count(case when a.status = com.medagenda.scheduling.AppointmentStatus.COMPLETED then 1 end),
                count(case when a.status = com.medagenda.scheduling.AppointmentStatus.NO_SHOW then 1 end),
                count(case when a.status = com.medagenda.scheduling.AppointmentStatus.CANCELLED then 1 end))
            from Appointment a
            join a.doctor d
            join d.specialty s
            where a.scheduledAt >= :from and a.scheduledAt < :to
            group by d.id, d.name, s.name
            order by count(a) desc, d.name
            """)
    List<com.medagenda.admin.report.DoctorReportRow> reportByDoctor(LocalDateTime from, LocalDateTime to);
}
