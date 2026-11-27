package com.medagenda.scheduling;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /**
     * Services ask this Clock for "now" instead of calling LocalDateTime.now() directly:
     * it pins the clinic's time zone, and tests can replace it with Clock.fixed(...) to check
     * time-based rules (future date, 24h notice) deterministically.
     */
    @Bean
    Clock clock(SchedulingProperties properties) {
        return Clock.system(properties.clinicZone());
    }
}
