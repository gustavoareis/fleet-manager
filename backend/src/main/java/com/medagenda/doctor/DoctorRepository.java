package com.medagenda.doctor;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /** The entity graph fetches specialty and user in the same SQL, avoiding N+1 queries in the list. */
    @EntityGraph(attributePaths = {"specialty", "user"})
    @Query("""
            select d from Doctor d
            where lower(d.name) like :namePattern
              and (:specialtyId is null or d.specialty.id = :specialtyId)
            """)
    Page<Doctor> search(String namePattern, Long specialtyId, Pageable pageable);

    @EntityGraph(attributePaths = {"specialty", "user"})
    Optional<Doctor> findWithDetailsById(Long id);

    boolean existsByCrm(String crm);

    boolean existsByCrmAndIdNot(String crm, Long id);

    boolean existsBySpecialtyId(Long specialtyId);
}
