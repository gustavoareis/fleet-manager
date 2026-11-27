package com.medagenda.specialty;

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
@RequestMapping("/api/specialties")
@Tag(name = "Especialidades")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    @Operation(summary = "Lista especialidades (paginado, filtro opcional por nome)")
    public PageResponse<SpecialtyResponse> search(
            @RequestParam(required = false) String name,
            @ParameterObject @SortDefault(sort = "name") Pageable pageable) {
        return specialtyService.search(name, Pageables.allowSort(pageable, "name"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma especialidade pelo id")
    public SpecialtyResponse findById(@PathVariable Long id) {
        return specialtyService.findById(id);
    }

    @PostMapping
    @StaffOnly
    @Operation(summary = "Cadastra uma especialidade")
    public ResponseEntity<SpecialtyResponse> create(
            @Valid @RequestBody SpecialtyRequest request, UriComponentsBuilder uriBuilder) {
        SpecialtyResponse created = specialtyService.create(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/specialties/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @StaffOnly
    @Operation(summary = "Atualiza uma especialidade")
    public SpecialtyResponse update(@PathVariable Long id, @Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @StaffOnly
    @Operation(summary = "Exclui uma especialidade sem médicos vinculados")
    public void delete(@PathVariable Long id) {
        specialtyService.delete(id);
    }
}
