package com.medagenda.patient;

import com.medagenda.auth.security.AdminOnly;
import com.medagenda.auth.security.StaffOnly;
import com.medagenda.common.web.PageResponse;
import com.medagenda.common.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Pacientes")
public class PatientController {

    /** Staff see any patient; a patient sees only their own record. */
    private static final String STAFF_OR_SELF = "hasAnyRole('ADMIN', 'RECEPTIONIST') or @access.isPatient(#id)";

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @StaffOnly
    @Operation(summary = "Lista pacientes (paginado, filtros opcionais por nome e início do CPF)")
    public PageResponse<PatientResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cpf,
            @ParameterObject @SortDefault(sort = "name") Pageable pageable) {
        return patientService.search(name, cpf, Pageables.allowSort(pageable, "name", "birthDate"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF_OR_SELF)
    @Operation(summary = "Busca um paciente pelo id")
    public PatientResponse findById(@PathVariable Long id) {
        return patientService.findById(id);
    }

    @PostMapping
    @StaffOnly
    @Operation(summary = "Cadastra um paciente e sua conta de acesso")
    public ResponseEntity<PatientResponse> create(
            @Valid @RequestBody PatientCreateRequest request, UriComponentsBuilder uriBuilder) {
        PatientResponse created = patientService.create(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/patients/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(STAFF_OR_SELF)
    @Operation(summary = "Atualiza os dados de um paciente (o CPF não é editável)")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AdminOnly
    @Operation(summary = "Exclui um paciente sem consultas registradas")
    public void delete(@PathVariable Long id) {
        patientService.delete(id);
    }
}
