package com.medagenda.scheduling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Agenda do médico")
public class DoctorAgendaController {

    /** Only the doctor who owns the appointment records its outcome or notes. */
    private static final String OWN_APPOINTMENT = "@access.isAppointmentDoctor(#id)";

    private final DoctorAgendaService agendaService;

    public DoctorAgendaController(DoctorAgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/api/doctors/{doctorId}/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') or @access.isDoctor(#doctorId)")
    @Operation(summary = "Agenda do médico entre duas datas (inclusive, no máximo 31 dias)")
    public List<DoctorAppointmentResponse> agenda(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return agendaService.agenda(doctorId, from, to);
    }

    @PatchMapping("/api/appointments/{id}/complete")
    @PreAuthorize(OWN_APPOINTMENT)
    @Operation(summary = "Marca a consulta como realizada (a partir do horário), com anotações opcionais")
    public DoctorAppointmentResponse complete(
            @PathVariable Long id, @Valid @RequestBody(required = false) NotesRequest request) {
        return agendaService.complete(id, request == null ? null : request.notes());
    }

    @PatchMapping("/api/appointments/{id}/no-show")
    @PreAuthorize(OWN_APPOINTMENT)
    @Operation(summary = "Marca que o paciente faltou (a partir do horário), com anotações opcionais")
    public DoctorAppointmentResponse noShow(
            @PathVariable Long id, @Valid @RequestBody(required = false) NotesRequest request) {
        return agendaService.markNoShow(id, request == null ? null : request.notes());
    }

    @PutMapping("/api/appointments/{id}/notes")
    @PreAuthorize(OWN_APPOINTMENT)
    @Operation(summary = "Grava as anotações clínicas da consulta (texto vazio apaga)")
    public DoctorAppointmentResponse notes(@PathVariable Long id, @Valid @RequestBody NotesRequest request) {
        return agendaService.saveNotes(id, request.notes());
    }
}
