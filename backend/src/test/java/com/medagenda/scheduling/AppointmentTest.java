package com.medagenda.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.medagenda.common.error.BusinessException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** State rules of the entity itself: no Spring, no database, "now" is always passed in. */
class AppointmentTest {

    private static final Duration NOTICE = Duration.ofHours(24);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 27, 10, 0);

    private static Appointment at(LocalDateTime when) {
        return new Appointment(null, null, when, NOW.minusDays(5));
    }

    @Test
    void newAppointmentStartsScheduledAndActive() {
        Appointment appointment = at(NOW.plusDays(3));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.isActive()).isTrue();
    }

    @Test
    void cancelsWithMoreThan24HoursNotice() {
        Appointment appointment = at(NOW.plusHours(25));

        appointment.cancel(NOW, NOTICE);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelsExactlyAt24HoursNotice() {
        Appointment appointment = at(NOW.plusHours(24));

        assertThat(appointment.canBeCancelled(NOW, NOTICE)).isTrue();
        appointment.cancel(NOW, NOTICE);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void refusesCancellationOneMinuteInsideTheNoticeWindow() {
        Appointment appointment = at(NOW.plusHours(24).minusMinutes(1));

        assertThat(appointment.canBeCancelled(NOW, NOTICE)).isFalse();
        assertThatThrownBy(() -> appointment.cancel(NOW, NOTICE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("24 horas");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void cannotCancelTwice() {
        Appointment appointment = at(NOW.plusDays(5));
        appointment.cancel(NOW, NOTICE);

        assertThatThrownBy(() -> appointment.cancel(NOW, NOTICE)).isInstanceOf(BusinessException.class);
    }

    @Test
    void confirmsArrivalOnlyOnTheSameDay() {
        Appointment today = at(NOW.plusHours(4));
        Appointment tomorrow = at(NOW.plusDays(1));

        today.confirmArrival(NOW);

        assertThat(today.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(tomorrow.canConfirmArrival(NOW)).isFalse();
        assertThatThrownBy(() -> tomorrow.confirmArrival(NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no dia da consulta");
    }

    @Test
    void cannotConfirmArrivalTwice() {
        Appointment appointment = at(NOW.plusHours(2));
        appointment.confirmArrival(NOW);

        assertThatThrownBy(() -> appointment.confirmArrival(NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    void confirmedAppointmentStillOccupiesTheSlot() {
        Appointment appointment = at(NOW.plusHours(2));
        appointment.confirmArrival(NOW);

        assertThat(appointment.isActive()).isTrue();
    }

    @Test
    void outcomeCannotBeRecordedBeforeTheAppointmentTime() {
        Appointment appointment = at(NOW.plusHours(1));

        assertThat(appointment.canRecordOutcome(NOW)).isFalse();
        assertThatThrownBy(() -> appointment.complete(NOW, "ok")).isInstanceOf(BusinessException.class);
    }

    @Test
    void completesAfterTheScheduledTimeAndStoresStrippedNotes() {
        Appointment appointment = at(NOW.minusHours(1));

        appointment.complete(NOW, "  Retorno em 30 dias  ");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(appointment.getNotes()).isEqualTo("Retorno em 30 dias");
    }

    @Test
    void marksNoShowAndStoresBlankNotesAsNull() {
        Appointment appointment = at(NOW.minusMinutes(30));
        appointment.updateNotes("antiga");

        appointment.markNoShow(NOW, "   ");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        assertThat(appointment.getNotes()).isNull();
    }

    @Test
    void finishedAppointmentCannotBeCancelledOrCompletedAgain() {
        Appointment appointment = at(NOW.minusHours(2));
        appointment.complete(NOW, null);

        assertThatThrownBy(() -> appointment.cancel(NOW, NOTICE)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> appointment.markNoShow(NOW, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    void notesCannotBeAddedToCancelledAppointment() {
        Appointment appointment = at(NOW.plusDays(3));
        appointment.cancel(NOW, NOTICE);

        assertThatThrownBy(() -> appointment.updateNotes("x")).isInstanceOf(BusinessException.class);
    }
}
