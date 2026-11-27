package com.medagenda.scheduling;

import com.medagenda.doctor.DoctorAvailability;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Turns availability windows into bookable start times. Pure function (no database, no clock),
 * so the slot arithmetic can be unit-tested in isolation.
 *
 * Example: 08:00-10:00 with 30 min -> 08:00, 08:30, 09:00, 09:30
 * (a slot is only offered if it fits entirely inside the window).
 */
public final class SlotSchedule {

    private SlotSchedule() {
    }

    public static List<LocalTime> startTimes(Collection<DoctorAvailability> windows) {
        TreeSet<LocalTime> starts = new TreeSet<>();
        for (DoctorAvailability window : windows) {
            starts.addAll(startTimes(window.getStartTime(), window.getEndTime(), window.getSlotDurationMinutes()));
        }
        return List.copyOf(starts);
    }

    static List<LocalTime> startTimes(LocalTime windowStart, LocalTime windowEnd, int slotMinutes) {
        TreeSet<LocalTime> starts = new TreeSet<>();
        // Minutes since midnight avoid LocalTime wrapping around at 24:00
        int end = windowEnd.toSecondOfDay() / 60;
        for (int start = windowStart.toSecondOfDay() / 60; start + slotMinutes <= end; start += slotMinutes) {
            starts.add(LocalTime.of(start / 60, start % 60));
        }
        return List.copyOf(starts);
    }
}
