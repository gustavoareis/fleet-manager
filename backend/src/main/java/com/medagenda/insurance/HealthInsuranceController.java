package com.medagenda.insurance;

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
@RequestMapping("/api/health-insurances")
@Tag(name = "Convênios")
public class HealthInsuranceController {

    private final HealthInsuranceService insuranceService;

    public HealthInsuranceController(HealthInsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @GetMapping
    @Operation(summary = "Lista convênios (paginado, filtros opcionais por nome e status)")
    public PageResponse<HealthInsuranceResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @ParameterObject @SortDefault(sort = "name") Pageable pageable) {
        return insuranceService.search(name, active, Pageables.allowSort(pageable, "name", "active"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um convênio pelo id")
    public HealthInsuranceResponse findById(@PathVariable Long id) {
        return insuranceService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um convênio")
    public ResponseEntity<HealthInsuranceResponse> create(
            @Valid @RequestBody HealthInsuranceRequest request, UriComponentsBuilder uriBuilder) {
        HealthInsuranceResponse created = insuranceService.create(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/health-insurances/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza (ou ativa/desativa) um convênio")
    public HealthInsuranceResponse update(@PathVariable Long id, @Valid @RequestBody HealthInsuranceRequest request) {
        return insuranceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um convênio sem pacientes vinculados")
    public void delete(@PathVariable Long id) {
        insuranceService.delete(id);
    }
}
