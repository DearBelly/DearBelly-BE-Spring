package com.hanium.mom4u.domain.member.repository;

import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BabyRepository extends JpaRepository<Baby, Long> {

    void deleteByMember(Member member);

    // 개인: 진행 중 아기 최신 1건 (LMP 있으면 LMP, 없으면 createdAt 기준으로도 무방)
    @Query("""
        SELECT b FROM Baby b
        WHERE b.member.id = :memberId AND b.isEnded = false
        ORDER BY COALESCE(b.lmpDate, b.createdAt) DESC
    """)
    List<Baby> findOngoingByMemberId(@Param("memberId") Long memberId, org.springframework.data.domain.Pageable pageable);

    // 가족: 진행 중 아기 최신 1건 (LMP 우선, 없으면 createdAt)
    @Query("""
        SELECT b FROM Baby b
        WHERE b.member.family.id = :familyId AND b.isEnded = false
        ORDER BY COALESCE(b.lmpDate, b.createdAt) DESC
    """)
    List<Baby> findOngoingByFamilyId(@Param("familyId") Long familyId, org.springframework.data.domain.Pageable pageable);
}
