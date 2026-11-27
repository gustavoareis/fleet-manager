package com.medagenda.admin;

import com.medagenda.scheduling.AppointmentStatus;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Appointment counters by status for a period (used by the dashboard and the report). */
public record StatusSummary(long total, long scheduled, long confirmed, long completed, long noShow, long cancelled) {

    /** Builds the summary from the rows of the grouped query (status, count). */
    public static StatusSummary from(List<Object[]> rows) {
        Map<AppointmentStatus, Long> counts = new EnumMap<>(AppointmentStatus.class);
        for (Object[] row : rows) {
            counts.put((AppointmentStatus) row[0], (Long) row[1]);
        }
        long total = counts.values().stream().mapToLong(count -> count).sum();
        return new StatusSummary(
                total,
                counts.getOrDefault(AppointmentStatus.SCHEDULED, 0L),
                counts.getOrDefault(AppointmentStatus.CONFIRMED, 0L),
                counts.getOrDefault(AppointmentStatus.COMPLETED, 0L),
                counts.getOrDefault(AppointmentStatus.NO_SHOW, 0L),
                counts.getOrDefault(AppointmentStatus.CANCELLED, 0L));
    }

    /** Patients who came to the clinic (checked in, or already seen). */
    public long arrived() {
        return confirmed + completed;
    }
}
