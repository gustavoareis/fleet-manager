package com.medagenda.auth;

import com.medagenda.auth.security.CurrentUser;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.doctor.DoctorRepository;
import com.medagenda.patient.PatientCreateRequest;
import com.medagenda.patient.PatientRepository;
import com.medagenda.patient.PatientService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final CurrentUser currentUser;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService,
                       UserRepository userRepository, PatientRepository patientRepository,
                       DoctorRepository doctorRepository, PatientService patientService, CurrentUser currentUser) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
        this.currentUser = currentUser;
    }

    /** Wrong e-mail or password throws BadCredentialsException, answered as 401 by the error handler. */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        User user = findUser(UserAccountService.normalizeEmail(request.email()));
        return AuthResponse.bearer(tokenService.issue(user), toUserInfo(user));
    }

    /** Patient self sign-up. Returns a token right away, so the new patient is already logged in. */
    @Transactional
    public AuthResponse registerPatient(PatientCreateRequest request) {
        patientService.create(request);
        User user = findUser(UserAccountService.normalizeEmail(request.email()));
        return AuthResponse.bearer(tokenService.issue(user), toUserInfo(user));
    }

    public UserInfo me() {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> NotFoundException.of("Usuário", currentUser.id()));
        return toUserInfo(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    private UserInfo toUserInfo(User user) {
        return switch (user.getRole()) {
            case PATIENT -> patientRepository.findByUserId(user.getId())
                    .map(p -> new UserInfo(user.getId(), user.getEmail(), user.getRole(), p.getName(), p.getId(), null))
                    .orElseGet(() -> withoutProfile(user));
            case DOCTOR -> doctorRepository.findByUserId(user.getId())
                    .map(d -> new UserInfo(user.getId(), user.getEmail(), user.getRole(), d.getName(), null, d.getId()))
                    .orElseGet(() -> withoutProfile(user));
            case RECEPTIONIST, ADMIN -> withoutProfile(user);
        };
    }

    /** Staff accounts have no profile table, so the e-mail doubles as display name. */
    private static UserInfo withoutProfile(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getRole(), user.getEmail(), null, null);
    }
}
