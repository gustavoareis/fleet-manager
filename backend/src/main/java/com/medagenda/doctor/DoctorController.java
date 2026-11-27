package com.medagenda.doctor;

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
@RequestMapping("/api/doctors")
@Tag(name = "Médicos")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    @Operation(summary = "Lista médicos (paginado, filtros opcionais por nome e especialidade)")
    public PageResponse<DoctorResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long specialtyId,
            @ParameterObject @SortDefault(sort = "name") Pageable pageable) {
        return doctorService.search(name, specialtyId, Pageables.allowSort(pageable, "name", "crm"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um médico pelo id")
    public DoctorResponse findById(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um médico e sua conta de acesso")
    public ResponseEntity<DoctorResponse> create(
            @Valid @RequestBody DoctorCreateRequest request, UriComponentsBuilder uriBuilder) {
        DoctorResponse created = doctorService.create(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/doctors/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um médico")
    public DoctorResponse update(@PathVariable Long id, @Valid @RequestBody DoctorUpdateRequest request) {
        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um médico sem consultas registradas")
    public void delete(@PathVariable Long id) {
        doctorService.delete(id);
    }
}
