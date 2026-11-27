package com.medagenda.auth;

import com.medagenda.patient.PatientCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** Logout has no endpoint: the API is stateless, so the client just discards the token. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements // public: no lock icon in Swagger
    @Operation(summary = "Autentica com e-mail e senha e retorna um JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Cadastro do próprio paciente; já retorna o JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody PatientCreateRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        AuthResponse response = authService.registerPatient(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/patients/{id}").buildAndExpand(response.user().patientId()).toUri())
                .body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public UserInfo me() {
        return authService.me();
    }
}
