package com.medagenda.doctor;

import com.medagenda.auth.Role;
import com.medagenda.auth.User;
import com.medagenda.auth.UserAccountService;
import com.medagenda.common.error.ConflictException;
import com.medagenda.common.error.NotFoundException;
import com.medagenda.common.web.PageResponse;
import com.medagenda.common.web.SearchTerms;
import com.medagenda.scheduling.AppointmentRepository;
import com.medagenda.specialty.Specialty;
import com.medagenda.specialty.SpecialtyService;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyService specialtyService;
    private final UserAccountService userAccountService;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, SpecialtyService specialtyService,
                         UserAccountService userAccountService, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.specialtyService = specialtyService;
        this.userAccountService = userAccountService;
        this.appointmentRepository = appointmentRepository;
    }

    public PageResponse<DoctorResponse> search(String name, Long specialtyId, Pageable pageable) {
        return PageResponse.from(
                doctorRepository.search(SearchTerms.contains(name), specialtyId, pageable), DoctorResponse::from);
    }

    public DoctorResponse findById(Long id) {
        return doctorRepository.findWithDetailsById(id)
                .map(DoctorResponse::from)
                .orElseThrow(() -> NotFoundException.of("Médico", id));
    }

    /** Account and profile are created in the same transaction: if either fails, neither is persisted. */
    @Transactional
    public DoctorResponse create(DoctorCreateRequest request) {
        String crm = normalizeCrm(request.crm());
        if (doctorRepository.existsByCrm(crm)) {
            throw new ConflictException("Já existe um médico com o CRM " + crm + ".");
        }
        Specialty specialty = specialtyService.getEntity(request.specialtyId());
        User user = userAccountService.createAccount(request.email(), request.password(), Role.DOCTOR);
        Doctor doctor = doctorRepository.save(new Doctor(request.name().strip(), crm, specialty, user));
        return DoctorResponse.from(doctor);
    }

    @Transactional
    public DoctorResponse update(Long id, DoctorUpdateRequest request) {
        Doctor doctor = getEntity(id);
        String crm = normalizeCrm(request.crm());
        if (doctorRepository.existsByCrmAndIdNot(crm, id)) {
            throw new ConflictException("Já existe um médico com o CRM " + crm + ".");
        }
        doctor.update(request.name().strip(), crm, specialtyService.getEntity(request.specialtyId()));
        return DoctorResponse.from(doctor);
    }

    /** Doctors with appointment history are kept: deleting them would erase medical records. */
    @Transactional
    public void delete(Long id) {
        Doctor doctor = getEntity(id);
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new ConflictException("O médico possui consultas registradas e não pode ser excluído.");
        }
        User user = doctor.getUser();
        doctorRepository.delete(doctor); // availabilities go with it (ON DELETE CASCADE)
        doctorRepository.flush();        // the doctor row must disappear before the user it references
        userAccountService.deleteAccount(user);
    }

    public Doctor getEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Médico", id));
    }

    private static String normalizeCrm(String crm) {
        return crm.strip().toUpperCase(Locale.ROOT);
    }
}
