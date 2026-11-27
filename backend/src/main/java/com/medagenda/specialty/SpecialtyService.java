package com.medagenda.specialty;

import com.medagenda.common.error.ConflictException;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.common.web.PageResponse;
import com.medagenda.common.web.SearchTerms;
import com.medagenda.doctor.DoctorRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services return DTOs, never entities: the transaction boundary ends here, so controllers
 * cannot trigger lazy loading by accident (open-in-view is disabled).
 */
@Service
@Transactional(readOnly = true)
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository, DoctorRepository doctorRepository) {
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
    }

    public PageResponse<SpecialtyResponse> search(String name, Pageable pageable) {
        return PageResponse.from(
                specialtyRepository.search(SearchTerms.contains(name), pageable), SpecialtyResponse::from);
    }

    public SpecialtyResponse findById(Long id) {
        return SpecialtyResponse.from(getEntity(id));
    }

    @Transactional
    public SpecialtyResponse create(SpecialtyRequest request) {
        String name = request.name().strip();
        if (specialtyRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe a especialidade " + name + ".");
        }
        return SpecialtyResponse.from(specialtyRepository.save(new Specialty(name)));
    }

    @Transactional
    public SpecialtyResponse update(Long id, SpecialtyRequest request) {
        Specialty specialty = getEntity(id);
        String name = request.name().strip();
        if (specialtyRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Já existe a especialidade " + name + ".");
        }
        // No save() needed: the entity is managed and changes are flushed on commit (dirty checking)
        specialty.rename(name);
        return SpecialtyResponse.from(specialty);
    }

    @Transactional
    public void delete(Long id) {
        Specialty specialty = getEntity(id);
        if (doctorRepository.existsBySpecialtyId(id)) {
            throw new ConflictException("A especialidade possui médicos vinculados e não pode ser excluída.");
        }
        specialtyRepository.delete(specialty);
    }

    /** For other features that need to reference a specialty. */
    public Specialty getEntity(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Especialidade", id));
    }
}
