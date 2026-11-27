package com.medagenda.insurance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {

    @Query("""
            select h from HealthInsurance h
            where lower(h.name) like :namePattern
              and (:active is null or h.active = :active)
            """)
    Page<HealthInsurance> search(String namePattern, Boolean active, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
