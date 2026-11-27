package com.medagenda.specialty;

public record SpecialtyResponse(Long id, String name) {

    public static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(specialty.getId(), specialty.getName());
    }
}
