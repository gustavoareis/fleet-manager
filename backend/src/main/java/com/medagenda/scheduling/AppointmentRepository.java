package com.medagenda.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorId(Long doctorId);

    boolean existsByPatientId(Long patientId);
}
