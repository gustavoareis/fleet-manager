package com.medagenda.auth.security;

import com.medagenda.doctor.DoctorRepository;
import com.medagenda.patient.PatientRepository;
import org.springframework.stereotype.Component;

/**
 * Ownership checks used in SpEL, e.g. {@code @PreAuthorize("@access.isPatient(#id)")}.
 * Roles answer "what kind of user is this?"; these answer "is this record theirs?".
 */
@Component("access")
public class AccessControl {

    private final CurrentUser currentUser;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AccessControl(CurrentUser currentUser, PatientRepository patientRepository,
                         DoctorRepository doctorRepository) {
        this.currentUser = currentUser;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /** True when the logged-in user is the patient with this id. */
    public boolean isPatient(Long patientId) {
        return patientRepository.existsByIdAndUserId(patientId, currentUser.id());
    }

    /** True when the logged-in user is the doctor with this id. */
    public boolean isDoctor(Long doctorId) {
        return doctorRepository.existsByIdAndUserId(doctorId, currentUser.id());
    }
}
