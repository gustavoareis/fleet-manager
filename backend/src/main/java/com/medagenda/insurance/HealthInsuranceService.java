package com.medagenda.insurance;

import com.medagenda.common.error.BusinessException;
import com.medagenda.common.error.ConflictException;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.common.web.PageResponse;
import com.medagenda.common.web.SearchTerms;
import com.medagenda.patient.PatientRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HealthInsuranceService {

    private final HealthInsuranceRepository insuranceRepository;
    private final PatientRepository patientRepository;

    public HealthInsuranceService(HealthInsuranceRepository insuranceRepository, PatientRepository patientRepository) {
        this.insuranceRepository = insuranceRepository;
        this.patientRepository = patientRepository;
    }

    public PageResponse<HealthInsuranceResponse> search(String name, Boolean active, Pageable pageable) {
        return PageResponse.from(
                insuranceRepository.search(SearchTerms.contains(name), active, pageable),
                HealthInsuranceResponse::from);
    }

    public HealthInsuranceResponse findById(Long id) {
        return HealthInsuranceResponse.from(getEntity(id));
    }

    @Transactional
    public HealthInsuranceResponse create(HealthInsuranceRequest request) {
        String name = request.name().strip();
        if (insuranceRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe o convênio " + name + ".");
        }
        return HealthInsuranceResponse.from(insuranceRepository.save(new HealthInsurance(name, request.active())));
    }

    @Transactional
    public HealthInsuranceResponse update(Long id, HealthInsuranceRequest request) {
        HealthInsurance insurance = getEntity(id);
        String name = request.name().strip();
        if (insuranceRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Já existe o convênio " + name + ".");
        }
        insurance.update(name, request.active());
        return HealthInsuranceResponse.from(insurance);
    }

    @Transactional
    public void delete(Long id) {
        HealthInsurance insurance = getEntity(id);
        if (patientRepository.existsByHealthInsuranceId(id)) {
            throw new ConflictException("O convênio possui pacientes vinculados. Desative-o em vez de excluir.");
        }
        insuranceRepository.delete(insurance);
    }

    public HealthInsurance getEntity(Long id) {
        return insuranceRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Convênio", id));
    }

    /** New links (patient sign-up or update) are only allowed to active insurances. */
    public HealthInsurance getActiveEntity(Long id) {
        HealthInsurance insurance = getEntity(id);
        if (!insurance.isActive()) {
            throw new BusinessException("O convênio " + insurance.getName() + " não está ativo.");
        }
        return insurance;
    }
}
