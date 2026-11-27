package com.medagenda.scheduling;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record FreeSlotsResponse(LocalDate date, List<LocalTime> slots) {
}
