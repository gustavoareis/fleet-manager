package com.medagenda.scheduling;

import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed view of the {@code medagenda.scheduling.*} settings. */
@ConfigurationProperties(prefix = "medagenda.scheduling")
public record SchedulingProperties(
        ZoneId clinicZone,
        int maxDaysAhead,
        Duration minCancellationNotice) {
}
