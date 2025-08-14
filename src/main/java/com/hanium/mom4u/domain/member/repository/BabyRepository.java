package com.hanium.mom4u.domain.member.repository;

import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BabyRepository extends JpaRepository<Baby,Long> {
    void deleteByMember(Member member);

    @Query("""
      SELECT b FROM Baby b
      WHERE b.member.id = :memberId AND b.isEnded = false
      ORDER BY b.createdAt DESC
    """)
    Optional<Baby> findCurrentByMemberId(Long memberId);


    @Query("""
        SELECT b FROM Baby b
        WHERE b.member.family.id = :familyId
          AND b.isEnded = false
        ORDER BY COALESCE(b.lmpDate, b.createdAt) DESC
    """)
    Optional<Baby> findCurrentByFamilyId(@Param("familyId") Long familyId);
}
