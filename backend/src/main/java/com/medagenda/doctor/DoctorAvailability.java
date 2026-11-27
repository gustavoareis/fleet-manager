package com.medagenda.doctor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** A recurring weekly window in which the doctor sees patients, split into fixed-length slots. */
@Entity
@Table(name = "doctor_availabilities")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 9)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration_minutes", nullable = false)
    private int slotDurationMinutes;

    protected DoctorAvailability() {
        // required by JPA
    }

    public DoctorAvailability(Doctor doctor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                              int slotDurationMinutes) {
        this.doctor = doctor;
        update(dayOfWeek, startTime, endTime, slotDurationMinutes);
    }

    public void update(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, int slotDurationMinutes) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }
}
