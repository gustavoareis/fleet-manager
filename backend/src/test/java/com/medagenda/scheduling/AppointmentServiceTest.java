package com.medagenda.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medagenda.auth.Role;
import com.medagenda.auth.security.CurrentUser;
import com.medagenda.common.error.BusinessException;
import com.medagenda.common.error.ConflictException;
import com.medagenda.doctor.Doctor;
import com.medagenda.doctor.DoctorAvailability;
import com.medagenda.doctor.DoctorAvailabilityRepository;
import com.medagenda.doctor.DoctorService;
import com.medagenda.patient.Patient;
import com.medagenda.patient.PatientRepository;
import com.medagenda.patient.PatientService;
import com.medagenda.specialty.Specialty;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Booking and cancellation rules with every collaborator mocked. The clock is frozen at
 * Friday 2026-11-27 10:00 (clinic time), so "future", "90 days" and "24 hours" are deterministic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 27, 10, 0);
    /** Monday, inside the doctor's 08:00-12:00 window (30 min slots). */
    private static final LocalDateTime MONDAY_9H = LocalDateTime.of(2026, 11, 30, 9, 0);

    @Mock AppointmentRepository appointmentRepository;
    @Mock DoctorAvailabilityRepository availabilityRepository;
    @Mock DoctorService doctorService;
    @Mock PatientService patientService;
    @Mock PatientRepository patientRepository;
    @Mock CurrentUser currentUser;

    private AppointmentService service;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        SchedulingProperties properties = new SchedulingProperties(ZONE, 90, Duration.ofHours(24));
        service = new AppointmentService(appointmentRepository, availabilityRepository, doctorService,
                patientService, patientRepository, currentUser, properties, clock);

        doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(10L);
        when(doctor.getName()).thenReturn("Dra. Ana");
        Specialty cardiology = mock(Specialty.class);
        when(cardiology.getName()).thenReturn("Cardiologia");
        when(doctor.getSpecialty()).thenReturn(cardiology);
        patient = mock(Patient.class);
        when(patient.getId()).thenReturn(20L);
        when(patient.getName()).thenReturn("Maria");

        when(doctorService.getEntity(10L)).thenReturn(doctor);
        when(currentUser.id()).thenReturn(99L);
        when(currentUser.hasRole(Role.PATIENT)).thenReturn(true);
        when(patientRepository.findByUserId(99L)).thenReturn(Optional.of(patient));

        DoctorAvailability window = mock(DoctorAvailability.class);
        when(window.getStartTime()).thenReturn(LocalTime.of(8, 0));
        when(window.getEndTime()).thenReturn(LocalTime.of(12, 0));
        when(window.getSlotDurationMinutes()).thenReturn(30);
        when(availabilityRepository.findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(eq(10L), eq(DayOfWeek.MONDAY)))
                .thenReturn(List.of(window));

        when(appointmentRepository.saveAndFlush(any(Appointment.class))).thenAnswer(call -> call.getArgument(0));
    }

    private AppointmentRequest request(LocalDateTime when) {
        return new AppointmentRequest(10L, when, null);
    }

    @Test
    void booksAFreeSlotInsideTheAvailability() {
        var response = service.book(request(MONDAY_9H));

        assertThat(response.scheduledAt()).isEqualTo(MONDAY_9H);
        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(appointmentRepository).saveAndFlush(any(Appointment.class));
    }

    @Test
    void refusesAPastDate() {
        assertThatThrownBy(() -> service.book(request(NOW.minusDays(1))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("futuros");
        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void refusesTheExactCurrentInstant() {
        assertThatThrownBy(() -> service.book(request(NOW))).isInstanceOf(BusinessException.class);
    }

    @Test
    void refusesADateBeyondTheBookingHorizon() {
        LocalDateTime tooFar = NOW.plusDays(91).withHour(9).withMinute(0);

        assertThatThrownBy(() -> service.book(request(tooFar)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("90 dias");
    }

    @Test
    void refusesTimeOutsideTheAvailabilityWindow() {
        assertThatThrownBy(() -> service.book(request(MONDAY_9H.withHour(14))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("disponibilidade");
    }

    @Test
    void refusesTimeOffTheSlotGrid() {
        assertThatThrownBy(() -> service.book(request(MONDAY_9H.withMinute(10))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("disponibilidade");
    }

    @Test
    void refusesADayWithoutAvailability() {
        LocalDateTime tuesday = MONDAY_9H.plusDays(1);

        assertThatThrownBy(() -> service.book(request(tuesday)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("disponibilidade");
    }

    @Test
    void refusesWhenTheDoctorAlreadyHasAnActiveAppointmentAtThatTime() {
        when(appointmentRepository.existsByDoctorIdAndScheduledAtAndStatusIn(
                eq(10L), eq(MONDAY_9H), eq(Appointment.ACTIVE_STATUSES))).thenReturn(true);

        assertThatThrownBy(() -> service.book(request(MONDAY_9H)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("reservado");
        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void refusesWhenThePatientAlreadyHasAnotherAppointmentAtThatTime() {
        when(appointmentRepository.existsByPatientIdAndScheduledAtAndStatusIn(
                eq(20L), eq(MONDAY_9H), eq(Appointment.ACTIVE_STATUSES))).thenReturn(true);

        assertThatThrownBy(() -> service.book(request(MONDAY_9H)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("paciente");
    }

    @Test
    void turnsALostRaceIntoAConflict() {
        // Both checks passed, but a concurrent request took the slot: the unique index rejects the INSERT
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
                .thenThrow(new DataIntegrityViolationException("uq_appointments_doctor_slot"));

        assertThatThrownBy(() -> service.book(request(MONDAY_9H))).isInstanceOf(ConflictException.class);
    }

    @Test
    void patientCannotBookForSomeoneElse() {
        service.book(new AppointmentRequest(10L, MONDAY_9H, 777L));

        verify(patientService, never()).getEntity(777L);
        verify(appointmentRepository).existsByPatientIdAndScheduledAtAndStatusIn(
                eq(20L), eq(MONDAY_9H), eq(Appointment.ACTIVE_STATUSES));
    }

    @Test
    void receptionMustInformThePatient() {
        when(currentUser.hasRole(Role.PATIENT)).thenReturn(false);

        assertThatThrownBy(() -> service.book(request(MONDAY_9H)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("paciente");
    }

    @Test
    void receptionBooksForTheChosenPatient() {
        when(currentUser.hasRole(Role.PATIENT)).thenReturn(false);
        when(patientService.getEntity(20L)).thenReturn(patient);

        var response = service.book(new AppointmentRequest(10L, MONDAY_9H, 20L));

        assertThat(response.scheduledAt()).isEqualTo(MONDAY_9H);
    }

    @Test
    void cancelsWhenThereIsAtLeast24HoursNotice() {
        Appointment appointment = new Appointment(patient, doctor, NOW.plusHours(24), NOW);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        service.cancel(1L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void refusesCancellationInsideTheNoticeWindow() {
        Appointment appointment = new Appointment(patient, doctor, NOW.plusHours(23), NOW);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancel(1L)).isInstanceOf(BusinessException.class);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void cancellingAnUnknownAppointmentIsNotFound() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(5L))
                .isInstanceOf(com.medagenda.common.error.NotFoundException.class);
    }
}
