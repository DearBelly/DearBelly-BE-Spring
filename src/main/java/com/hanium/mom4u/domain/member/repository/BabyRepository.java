package com.hanium.mom4u.domain.member.repository;

import com.hanium.mom4u.domain.member.entity.Baby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BabyRepository extends JpaRepository<Baby,Long> {
}
