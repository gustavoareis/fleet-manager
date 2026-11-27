package com.medagenda.insurance;

public record HealthInsuranceResponse(Long id, String name, boolean active) {

    public static HealthInsuranceResponse from(HealthInsurance insurance) {
        return new HealthInsuranceResponse(insurance.getId(), insurance.getName(), insurance.isActive());
    }
}
