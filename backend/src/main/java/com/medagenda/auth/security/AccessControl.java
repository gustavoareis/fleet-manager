package com.medagenda.auth.security;

import com.medagenda.doctor.DoctorRepository;
import com.medagenda.patient.PatientRepository;
import com.medagenda.scheduling.AppointmentRepository;
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
    private final AppointmentRepository appointmentRepository;

    public AccessControl(CurrentUser currentUser, PatientRepository patientRepository,
                         DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.currentUser = currentUser;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /** True when the logged-in user is the patient with this id. */
    public boolean isPatient(Long patientId) {
        return patientRepository.existsByIdAndUserId(patientId, currentUser.id());
    }

    /** True when the logged-in user is the doctor with this id. */
    public boolean isDoctor(Long doctorId) {
        return doctorRepository.existsByIdAndUserId(doctorId, currentUser.id());
    }

    /** True when the appointment is on the logged-in doctor's agenda. */
    public boolean isAppointmentDoctor(Long appointmentId) {
        return appointmentRepository.existsByIdAndDoctorUserId(appointmentId, currentUser.id());
    }

    /** True when the appointment belongs to the logged-in patient. */
    public boolean isAppointmentPatient(Long appointmentId) {
        return appointmentRepository.existsByIdAndPatientUserId(appointmentId, currentUser.id());
    }
}
