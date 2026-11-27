package com.medagenda.patient;

import com.medagenda.insurance.HealthInsuranceResponse;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String name,
        String cpf,
        String phone,
        LocalDate birthDate,
        HealthInsuranceResponse healthInsurance,
        String email) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getCpf(),
                patient.getPhone(),
                patient.getBirthDate(),
                patient.getHealthInsurance() == null ? null : HealthInsuranceResponse.from(patient.getHealthInsurance()),
                patient.getUser().getEmail());
    }
}
