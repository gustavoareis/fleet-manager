package com.medagenda.scheduling;

public enum AppointmentStatus {
    /** Booked by the patient. */
    SCHEDULED,
    /** Patient arrived at the clinic (confirmed by the reception). */
    CONFIRMED,
    CANCELLED,
    /** Doctor marked it as done. */
    COMPLETED,
    /** Doctor marked that the patient did not show up. */
    NO_SHOW
}
