package com.hanium.mom4u.domain.family.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findByCode(String code);

    boolean existsByCode(String code);
}
