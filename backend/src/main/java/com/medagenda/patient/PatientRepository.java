package com.medagenda.patient;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    /** The CPF filter is a prefix match, so the reception can type the first digits. */
    @EntityGraph(attributePaths = {"healthInsurance", "user"})
    @Query("""
            select p from Patient p
            where lower(function('unaccent', p.name)) like :namePattern
              and p.cpf like :cpfPrefix
            """)
    Page<Patient> search(String namePattern, String cpfPrefix, Pageable pageable);

    @EntityGraph(attributePaths = {"healthInsurance", "user"})
    Optional<Patient> findWithDetailsById(Long id);

    Optional<Patient> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByCpf(String cpf);

    boolean existsByHealthInsuranceId(Long healthInsuranceId);
}
