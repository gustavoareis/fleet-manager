package com.medagenda.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Availability is a sub-resource: it only exists in the context of a doctor. */
@RestController
@RequestMapping("/api/doctors/{doctorId}/availabilities")
@Tag(name = "Disponibilidade dos médicos")
public class DoctorAvailabilityController {

    /** Staff manage any agenda; a doctor manages only their own. */
    private static final String CAN_MANAGE = "hasAnyRole('ADMIN', 'RECEPTIONIST') or @access.isDoctor(#doctorId)";

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    @Operation(summary = "Lista as janelas semanais de atendimento do médico")
    public List<AvailabilityResponse> list(@PathVariable Long doctorId) {
        return availabilityService.list(doctorId);
    }

    @PostMapping
    @PreAuthorize(CAN_MANAGE)
    @Operation(summary = "Adiciona uma janela de atendimento")
    public ResponseEntity<AvailabilityResponse> create(
            @PathVariable Long doctorId, @Valid @RequestBody AvailabilityRequest request,
            UriComponentsBuilder uriBuilder) {
        AvailabilityResponse created = availabilityService.create(doctorId, request);
        return ResponseEntity
                .created(uriBuilder.path("/api/doctors/{doctorId}/availabilities/{id}")
                        .buildAndExpand(doctorId, created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{availabilityId}")
    @PreAuthorize(CAN_MANAGE)
    @Operation(summary = "Altera uma janela de atendimento")
    public AvailabilityResponse update(
            @PathVariable Long doctorId, @PathVariable Long availabilityId,
            @Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.update(doctorId, availabilityId, request);
    }

    @DeleteMapping("/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(CAN_MANAGE)
    @Operation(summary = "Remove uma janela de atendimento")
    public void delete(@PathVariable Long doctorId, @PathVariable Long availabilityId) {
        availabilityService.delete(doctorId, availabilityId);
    }
}
