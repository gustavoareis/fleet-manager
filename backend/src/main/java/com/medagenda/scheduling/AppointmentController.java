package com.medagenda.scheduling;

import com.medagenda.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Tag(name = "Agendamento")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final FreeSlotService freeSlotService;

    public AppointmentController(AppointmentService appointmentService, FreeSlotService freeSlotService) {
        this.appointmentService = appointmentService;
        this.freeSlotService = freeSlotService;
    }

    @GetMapping("/api/doctors/{doctorId}/free-slots")
    @Operation(summary = "Horários livres de um médico em uma data")
    public FreeSlotsResponse freeSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return freeSlotService.freeSlots(doctorId, date);
    }

    @PostMapping("/api/appointments")
    @PreAuthorize("hasAnyRole('PATIENT', 'RECEPTIONIST', 'ADMIN')")
    @Operation(summary = "Agenda uma consulta (paciente para si; recepção para um paciente)")
    public ResponseEntity<AppointmentResponse> book(
            @Valid @RequestBody AppointmentRequest request, UriComponentsBuilder uriBuilder) {
        AppointmentResponse created = appointmentService.book(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/appointments/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @GetMapping("/api/appointments/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Consultas do paciente logado: próximas (UPCOMING) ou histórico (HISTORY)")
    public PageResponse<AppointmentResponse> myAppointments(
            @RequestParam(defaultValue = "UPCOMING") AppointmentService.Scope scope,
            @ParameterObject Pageable pageable) {
        return appointmentService.myAppointments(scope, pageable);
    }

    /** PATCH: changes the state of the appointment; the record itself stays as history. */
    @PatchMapping("/api/appointments/{id}/cancel")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN') or @access.isAppointmentPatient(#id)")
    @Operation(summary = "Cancela uma consulta (mínimo de 24h de antecedência)")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return appointmentService.cancel(id);
    }
}
