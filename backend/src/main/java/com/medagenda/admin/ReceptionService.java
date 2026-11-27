package com.medagenda.admin;

import com.medagenda.common.error.NotFoundException;
import com.medagenda.common.web.PageResponse;
import com.medagenda.scheduling.Appointment;
import com.medagenda.scheduling.AppointmentRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases of the reception desk: today's list, patient check-in, daily counters. */
@Service
@Transactional(readOnly = true)
public class ReceptionService {

    static final int PAGE_SIZE = 15;

    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    public ReceptionService(AppointmentRepository appointmentRepository, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    /** All doctors' appointments of one day, soonest first; doctorId = null means every doctor. */
    public PageResponse<ReceptionRow> listDay(LocalDate date, Long doctorId, int page) {
        LocalDateTime now = LocalDateTime.now(clock);
        var result = appointmentRepository.findForReception(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay(), doctorId,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("scheduledAt", "id")));
        return PageResponse.from(result, appointment -> ReceptionRow.from(appointment, now));
    }

    public StatusSummary summary(LocalDate date) {
        return StatusSummary.from(appointmentRepository.countByStatus(date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
    }

    /** The patient arrived at the clinic: SCHEDULED -> CONFIRMED. The doctor then sees them as waiting. */
    @Transactional
    public String confirmArrival(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> NotFoundException.of("Consulta", appointmentId));
        appointment.confirmArrival(LocalDateTime.now(clock));
        return appointment.getPatient().getName();
    }
}
