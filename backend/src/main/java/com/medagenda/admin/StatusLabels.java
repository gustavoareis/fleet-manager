package com.medagenda.admin;

import com.medagenda.scheduling.AppointmentStatus;

/** Portuguese labels for the JSP pages (the Angular app has its own copy). */
public final class StatusLabels {

    private StatusLabels() {
    }

    public static String of(AppointmentStatus status) {
        return switch (status) {
            case SCHEDULED -> "Agendada";
            case CONFIRMED -> "Chegada confirmada";
            case CANCELLED -> "Cancelada";
            case COMPLETED -> "Realizada";
            case NO_SHOW -> "Não compareceu";
        };
    }
}
