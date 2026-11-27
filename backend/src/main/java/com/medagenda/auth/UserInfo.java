package com.medagenda.auth;

/**
 * What the frontend needs to know about the logged-in user. {@code patientId}/{@code doctorId}
 * are filled only for the matching role, so the app can call e.g. /api/patients/{patientId}.
 */
public record UserInfo(
        Long id,
        String email,
        Role role,
        String displayName,
        Long patientId,
        Long doctorId) {
}
