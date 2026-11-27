package com.medagenda.doctor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(Long doctorId, DayOfWeek dayOfWeek);

    Optional<DoctorAvailability> findByIdAndDoctorId(Long id, Long doctorId);

    /**
     * Two windows overlap when each one starts before the other ends.
     * {@code excludedId} lets an update ignore the window being edited (pass -1 on create).
     */
    @Query("""
            select count(a) > 0 from DoctorAvailability a
            where a.doctor.id = :doctorId
              and a.dayOfWeek = :dayOfWeek
              and a.id <> :excludedId
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    boolean existsOverlap(Long doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                          Long excludedId);
}
