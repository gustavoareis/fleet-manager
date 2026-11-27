package com.medagenda.doctor;

import com.medagenda.specialty.SpecialtyResponse;

public record DoctorResponse(Long id, String name, String crm, SpecialtyResponse specialty, String email) {

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getCrm(),
                SpecialtyResponse.from(doctor.getSpecialty()),
                doctor.getUser().getEmail());
    }
}
