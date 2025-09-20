package com.hanium.mom4u.domain.member.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByFamily(Family family);

    Optional<Member> findByEmailAndSocialTypeAndIsInactiveFalse(String email, SocialType socialType);

    List<Member> findAllByIsInactiveTrueAndInactiveDateBefore(LocalDate date);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.family.id = :familyId")
    long countByFamilyId(@Param("familyId") Long familyId);

    @Query("""
        select distinct m
        from Member m
        left join fetch m.family f
        left join fetch f.memberList ml
        where m.id = :id
    """)
    Optional<Member> findWithFamilyAndMembers(@Param("id") Long id);
}