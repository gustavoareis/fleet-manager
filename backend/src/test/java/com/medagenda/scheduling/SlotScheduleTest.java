package com.medagenda.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class SlotScheduleTest {

    @Test
    void splitsTheWindowIntoSlots() {
        assertThat(SlotSchedule.startTimes(LocalTime.of(8, 0), LocalTime.of(10, 0), 30))
                .containsExactly(LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30));
    }

    @Test
    void dropsTheLastSlotThatDoesNotFitInsideTheWindow() {
        // 08:00-09:50 with 30 min: 09:30 would end at 10:00, past the window
        assertThat(SlotSchedule.startTimes(LocalTime.of(8, 0), LocalTime.of(9, 50), 30))
                .containsExactly(LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0));
    }

    @Test
    void windowShorterThanOneSlotHasNoSlots() {
        assertThat(SlotSchedule.startTimes(LocalTime.of(8, 0), LocalTime.of(8, 20), 30)).isEmpty();
    }

    @Test
    void windowEndingNearMidnightDoesNotWrapAround() {
        assertThat(SlotSchedule.startTimes(LocalTime.of(22, 0), LocalTime.of(23, 59), 60))
                .containsExactly(LocalTime.of(22, 0));
    }

    @Test
    void stepsOddDurations() {
        assertThat(SlotSchedule.startTimes(LocalTime.of(9, 0), LocalTime.of(10, 0), 20))
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 20), LocalTime.of(9, 40));
    }
}
