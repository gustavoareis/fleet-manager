package com.medagenda.patient;

import com.medagenda.auth.Role;
import com.medagenda.auth.User;
import com.medagenda.auth.UserAccountService;
import com.medagenda.common.error.ConflictException;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.common.web.PageResponse;
import com.medagenda.common.web.SearchTerms;
import com.medagenda.insurance.HealthInsurance;
import com.medagenda.insurance.HealthInsuranceService;
import com.medagenda.scheduling.AppointmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final HealthInsuranceService insuranceService;
    private final UserAccountService userAccountService;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository, HealthInsuranceService insuranceService,
                          UserAccountService userAccountService, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.insuranceService = insuranceService;
        this.userAccountService = userAccountService;
        this.appointmentRepository = appointmentRepository;
    }

    public PageResponse<PatientResponse> search(String name, String cpf, Pageable pageable) {
        String cpfPrefix = (cpf == null ? "" : cpf.replaceAll("\\D", "")) + "%";
        return PageResponse.from(
                patientRepository.search(SearchTerms.contains(name), cpfPrefix, pageable), PatientResponse::from);
    }

    public PatientResponse findById(Long id) {
        return patientRepository.findWithDetailsById(id)
                .map(PatientResponse::from)
                .orElseThrow(() -> NotFoundException.of("Paciente", id));
    }

    @Transactional
    public PatientResponse create(PatientCreateRequest request) {
        if (patientRepository.existsByCpf(request.cpf())) {
            throw new ConflictException("Já existe um paciente com este CPF.");
        }
        HealthInsurance insurance = resolveInsurance(request.healthInsuranceId());
        User user = userAccountService.createAccount(request.email(), request.password(), Role.PATIENT);
        Patient patient = patientRepository.save(new Patient(request.name().strip(), request.cpf(),
                request.phone(), request.birthDate(), insurance, user));
        return PatientResponse.from(patient);
    }

    @Transactional
    public PatientResponse update(Long id, PatientUpdateRequest request) {
        Patient patient = getEntity(id);
        HealthInsurance insurance = resolveInsurance(request.healthInsuranceId());
        patient.update(request.name().strip(), request.phone(), request.birthDate(), insurance);
        return PatientResponse.from(patient);
    }

    @Transactional
    public void delete(Long id) {
        Patient patient = getEntity(id);
        if (appointmentRepository.existsByPatientId(id)) {
            throw new ConflictException("O paciente possui consultas registradas e não pode ser excluído.");
        }
        User user = patient.getUser();
        patientRepository.delete(patient);
        patientRepository.flush(); // the patient row must disappear before the user it references
        userAccountService.deleteAccount(user);
    }

    public Patient getEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Paciente", id));
    }

    private HealthInsurance resolveInsurance(Long insuranceId) {
        return insuranceId == null ? null : insuranceService.getActiveEntity(insuranceId);
    }
}
