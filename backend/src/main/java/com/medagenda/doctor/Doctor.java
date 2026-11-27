package com.medagenda.doctor;

import com.medagenda.auth.User;
import com.medagenda.specialty.Specialty;
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

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    /** Format "123456/SP": registration number + state. */
    @Column(nullable = false, unique = true, length = 12)
    private String crm;

    // LAZY everywhere: JPA's default for *ToOne is EAGER, which silently loads the whole graph
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    protected Doctor() {
        // required by JPA
    }

    public Doctor(String name, String crm, Specialty specialty, User user) {
        this.name = name;
        this.crm = crm;
        this.specialty = specialty;
        this.user = user;
    }

    public void update(String name, String crm, Specialty specialty) {
        this.name = name;
        this.crm = crm;
        this.specialty = specialty;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCrm() {
        return crm;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public User getUser() {
        return user;
    }
}
