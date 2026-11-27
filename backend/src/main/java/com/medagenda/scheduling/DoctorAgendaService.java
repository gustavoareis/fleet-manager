package com.medagenda.scheduling;

import com.medagenda.auth.Role;
import com.medagenda.auth.security.CurrentUser;
import com.medagenda.common.error.BadRequestException;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.doctor.DoctorService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DoctorAgendaService {

    /** A week plus margin; keeps one request from pulling the whole history. */
    static final int MAX_RANGE_DAYS = 31;

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final CurrentUser currentUser;
    private final Clock clock;

    public DoctorAgendaService(AppointmentRepository appointmentRepository, DoctorService doctorService,
                               CurrentUser currentUser, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    /**
     * Agenda for the dates [from, to] (both inclusive). Not paginated: the range is capped at
     * {@value #MAX_RANGE_DAYS} days, so the result is naturally small.
     */
    public List<DoctorAppointmentResponse> agenda(Long doctorId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BadRequestException("A data final não pode ser anterior à data inicial.");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new BadRequestException("O período consultado pode ter no máximo %d dias.".formatted(MAX_RANGE_DAYS));
        }
        doctorService.getEntity(doctorId); // 404 for an unknown doctor
        LocalDateTime now = LocalDateTime.now(clock);
        boolean includeNotes = currentUser.hasRole(Role.DOCTOR);
        return appointmentRepository
                .findAgenda(doctorId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .map(appointment -> DoctorAppointmentResponse.from(appointment, now, includeNotes))
                .toList();
    }

    /** Authorization (the doctor who owns the appointment) is checked by @PreAuthorize on the controller. */
    @Transactional
    public DoctorAppointmentResponse complete(Long appointmentId, String notes) {
        LocalDateTime now = LocalDateTime.now(clock);
        Appointment appointment = getAppointment(appointmentId);
        appointment.complete(now, notes);
        return DoctorAppointmentResponse.from(appointment, now, true);
    }

    @Transactional
    public DoctorAppointmentResponse markNoShow(Long appointmentId, String notes) {
        LocalDateTime now = LocalDateTime.now(clock);
        Appointment appointment = getAppointment(appointmentId);
        appointment.markNoShow(now, notes);
        return DoctorAppointmentResponse.from(appointment, now, true);
    }

    @Transactional
    public DoctorAppointmentResponse saveNotes(Long appointmentId, String notes) {
        Appointment appointment = getAppointment(appointmentId);
        appointment.updateNotes(notes);
        return DoctorAppointmentResponse.from(appointment, LocalDateTime.now(clock), true);
    }

    private Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id).orElseThrow(() -> NotFoundException.of("Consulta", id));
    }
}
