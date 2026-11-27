package com.medagenda.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.medagenda.doctor.Doctor;
import com.medagenda.doctor.DoctorRepository;
import com.medagenda.patient.Patient;
import com.medagenda.patient.PatientRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database-level guarantees, against real PostgreSQL with the dev seed. Every test is
 * {@code @Transactional}, so Spring rolls it back and the seed stays untouched.
 */
@SpringBootTest
@Transactional
class AppointmentPersistenceIntegrationTest {

    @Autowired AppointmentRepository appointments;
    @Autowired DoctorRepository doctors;
    @Autowired PatientRepository patients;
    @Autowired Clock clock;

    private final LocalDateTime slot = LocalDateTime.of(2031, 3, 3, 9, 0);

    private Doctor anyDoctor() {
        return doctors.findAll().getFirst();
    }

    private List<Patient> twoPatients() {
        return patients.findAll().subList(0, 2);
    }

    @Test
    void uniqueIndexBlocksTheSameDoctorAtTheSameTime() {
        Doctor doctor = anyDoctor();
        List<Patient> two = twoPatients();
        appointments.saveAndFlush(new Appointment(two.get(0), doctor, slot, LocalDateTime.now(clock)));

        assertThatThrownBy(() ->
                appointments.saveAndFlush(new Appointment(two.get(1), doctor, slot, LocalDateTime.now(clock))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueIndexBlocksTheSamePatientAtTheSameTimeWithAnotherDoctor() {
        List<Doctor> docs = doctors.findAll();
        Patient patient = twoPatients().getFirst();
        appointments.saveAndFlush(new Appointment(patient, docs.get(0), slot, LocalDateTime.now(clock)));

        assertThatThrownBy(() ->
                appointments.saveAndFlush(new Appointment(patient, docs.get(1), slot, LocalDateTime.now(clock))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void cancelledAppointmentReleasesTheSlot() {
        Doctor doctor = anyDoctor();
        List<Patient> two = twoPatients();
        Appointment first = appointments.saveAndFlush(
                new Appointment(two.get(0), doctor, slot, LocalDateTime.now(clock)));
        assertThat(appointments.existsByDoctorIdAndScheduledAtAndStatusIn(
                doctor.getId(), slot, Appointment.ACTIVE_STATUSES)).isTrue();

        first.cancel(LocalDateTime.now(clock), java.time.Duration.ofHours(24));
        appointments.flush();

        assertThat(appointments.existsByDoctorIdAndScheduledAtAndStatusIn(
                doctor.getId(), slot, Appointment.ACTIVE_STATUSES)).isFalse();
        // The partial index only covers active appointments, so the slot can be booked again
        appointments.saveAndFlush(new Appointment(two.get(1), doctor, slot, LocalDateTime.now(clock)));
    }

    @Test
    void doctorSearchIgnoresAccentsAndCase() {
        // Seed has "Dr. Fábio Souza"; the user types without the accent, in any case
        var found = doctors.search("%fabio%", null, PageRequest.of(0, 10));

        assertThat(found.getContent()).extracting(doctor -> doctor.getName()).anyMatch(name -> name.contains("Fábio"));
    }
}
