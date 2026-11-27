package com.medagenda.scheduling;

import com.medagenda.auth.Role;
import com.medagenda.auth.security.CurrentUser;
import com.medagenda.common.error.BusinessException;
import com.medagenda.common.error.ConflictException;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.common.web.PageResponse;
import com.medagenda.doctor.Doctor;
import com.medagenda.doctor.DoctorAvailabilityRepository;
import com.medagenda.doctor.DoctorService;
import com.medagenda.patient.Patient;
import com.medagenda.patient.PatientRepository;
import com.medagenda.patient.PatientService;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    public enum Scope { UPCOMING, HISTORY }

    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final CurrentUser currentUser;
    private final SchedulingProperties properties;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorAvailabilityRepository availabilityRepository, DoctorService doctorService,
                              PatientService patientService, PatientRepository patientRepository,
                              CurrentUser currentUser, SchedulingProperties properties, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.currentUser = currentUser;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Booking rules, checked from cheapest to most expensive:
     * 1. future date, inside the booking horizon
     * 2. inside the doctor's availability and on the slot grid (e.g. 09:00, 09:30 - not 09:10)
     * 3. doctor free at that time
     * 4. patient free at that time
     * Rules 3 and 4 are also guaranteed by unique indexes, which catch the race where two
     * requests pass the checks at the same moment.
     */
    @Transactional
    public AppointmentResponse book(AppointmentRequest request) {
        Patient patient = resolvePatient(request.patientId());
        Doctor doctor = doctorService.getEntity(request.doctorId());
        LocalDateTime scheduledAt = request.scheduledAt();
        LocalDateTime now = LocalDateTime.now(clock);

        if (!scheduledAt.isAfter(now)) {
            throw new BusinessException("A consulta deve ser agendada para uma data e horário futuros.");
        }
        if (scheduledAt.toLocalDate().isAfter(now.toLocalDate().plusDays(properties.maxDaysAhead()))) {
            throw new BusinessException("Só é possível agendar com até %d dias de antecedência."
                    .formatted(properties.maxDaysAhead()));
        }
        var windows = availabilityRepository.findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(
                doctor.getId(), scheduledAt.getDayOfWeek());
        if (scheduledAt.getSecond() != 0 || scheduledAt.getNano() != 0
                || !SlotSchedule.startTimes(windows).contains(scheduledAt.toLocalTime())) {
            throw new BusinessException("O horário escolhido está fora da disponibilidade do médico.");
        }
        if (appointmentRepository.existsByDoctorIdAndScheduledAtAndStatusIn(
                doctor.getId(), scheduledAt, Appointment.ACTIVE_STATUSES)) {
            throw new ConflictException("Este horário acabou de ser reservado. Escolha outro horário.");
        }
        if (appointmentRepository.existsByPatientIdAndScheduledAtAndStatusIn(
                patient.getId(), scheduledAt, Appointment.ACTIVE_STATUSES)) {
            throw new ConflictException("O paciente já possui uma consulta neste horário.");
        }

        try {
            Appointment saved = appointmentRepository.saveAndFlush(
                    new Appointment(patient, doctor, scheduledAt, now));
            return toResponse(saved, now);
        } catch (DataIntegrityViolationException raceLost) {
            // Another request booked the same slot between our checks and the INSERT
            throw new ConflictException("Este horário acabou de ser reservado. Escolha outro horário.");
        }
    }

    /** Authorization (owner patient or staff) is checked by @PreAuthorize on the controller. */
    @Transactional
    public AppointmentResponse cancel(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Consulta", id));
        LocalDateTime now = LocalDateTime.now(clock);
        appointment.cancel(now, properties.minCancellationNotice());
        return toResponse(appointment, now);
    }

    /** Upcoming: soonest first. History: most recent first. */
    public PageResponse<AppointmentResponse> myAppointments(Scope scope, Pageable pageable) {
        Patient patient = currentPatient();
        LocalDateTime now = LocalDateTime.now(clock);
        Page<Appointment> page = switch (scope) {
            case UPCOMING -> appointmentRepository.findUpcomingByPatient(patient.getId(), now,
                    Appointment.ACTIVE_STATUSES, withSort(pageable, Sort.Direction.ASC));
            case HISTORY -> appointmentRepository.findHistoryByPatient(patient.getId(), now,
                    Appointment.ACTIVE_STATUSES, withSort(pageable, Sort.Direction.DESC));
        };
        return PageResponse.from(page, appointment -> toResponse(appointment, now));
    }

    /**
     * A patient always books for themself (any patientId in the body is ignored, so one patient
     * can't book for another). Staff book on behalf of a patient and must say which one.
     */
    private Patient resolvePatient(Long requestedPatientId) {
        if (currentUser.hasRole(Role.PATIENT)) {
            return currentPatient();
        }
        if (requestedPatientId == null) {
            throw new BusinessException("Informe o paciente da consulta.");
        }
        return patientService.getEntity(requestedPatientId);
    }

    private Patient currentPatient() {
        return patientRepository.findByUserId(currentUser.id())
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado para o usuário logado."));
    }

    private AppointmentResponse toResponse(Appointment appointment, LocalDateTime now) {
        return AppointmentResponse.from(appointment,
                appointment.canBeCancelled(now, properties.minCancellationNotice()));
    }

    private static Pageable withSort(Pageable pageable, Sort.Direction direction) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, "scheduledAt"));
    }
}
