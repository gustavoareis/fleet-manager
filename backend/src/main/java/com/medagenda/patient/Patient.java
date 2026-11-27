package com.medagenda.patient;

import com.medagenda.auth.User;
import com.medagenda.insurance.HealthInsurance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    /** Digits only, so searching and uniqueness don't depend on formatting. */
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** Null means a private ("particular") patient. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_insurance_id")
    private HealthInsurance healthInsurance;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    protected Patient() {
        // required by JPA
    }

    public Patient(String name, String cpf, String phone, LocalDate birthDate, HealthInsurance healthInsurance,
                   User user) {
        this.cpf = cpf;
        this.user = user;
        update(name, phone, birthDate, healthInsurance);
    }

    /** CPF is the patient's identity and is not editable. */
    public void update(String name, String phone, LocalDate birthDate, HealthInsurance healthInsurance) {
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.healthInsurance = healthInsurance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public HealthInsurance getHealthInsurance() {
        return healthInsurance;
    }

    public User getUser() {
        return user;
    }
}
