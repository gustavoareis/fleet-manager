package com.medagenda.scheduling;

import com.medagenda.doctor.DoctorAvailabilityRepository;
import com.medagenda.doctor.DoctorService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FreeSlotService {

    private final DoctorService doctorService;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final SchedulingProperties properties;
    private final Clock clock;

    public FreeSlotService(DoctorService doctorService, DoctorAvailabilityRepository availabilityRepository,
                           AppointmentRepository appointmentRepository, SchedulingProperties properties,
                           Clock clock) {
        this.doctorService = doctorService;
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Free slots = slots generated from the doctor's windows for that weekday
     * minus slots already booked minus slots that are already in the past.
     * Dates outside [today, today + max-days-ahead] simply have no free slots.
     */
    public FreeSlotsResponse freeSlots(Long doctorId, LocalDate date) {
        doctorService.getEntity(doctorId); // 404 for an unknown doctor
        LocalDateTime now = LocalDateTime.now(clock);
        if (!isBookableDate(date, now.toLocalDate())) {
            return new FreeSlotsResponse(date, List.of());
        }

        List<LocalTime> allSlots = SlotSchedule.startTimes(
                availabilityRepository.findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctorId, date.getDayOfWeek()));
        Set<LocalDateTime> booked = new HashSet<>(appointmentRepository.findBookedTimes(
                doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), Appointment.ACTIVE_STATUSES));

        List<LocalTime> free = allSlots.stream()
                .filter(time -> date.atTime(time).isAfter(now))
                .filter(time -> !booked.contains(date.atTime(time)))
                .toList();
        return new FreeSlotsResponse(date, free);
    }

    boolean isBookableDate(LocalDate date, LocalDate today) {
        return !date.isBefore(today) && !date.isAfter(today.plusDays(properties.maxDaysAhead()));
    }
}
