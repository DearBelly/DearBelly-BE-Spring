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

    List<Member> findAllByIsInactiveTrueAndInactiveDateBefore(LocalDate date);

    @Query("select m from Member m left join fetch m.family where m.id = :id")
    Optional<Member> findByIdWithFamily(@Param("id") Long id);

    @Query("select m.family.id from Member m where m.id = :memberId")
    Optional<Long> findFamilyIdByMemberId(Long memberId);

    Optional<Member> findBySocialTypeAndProviderIdAndIsInactiveFalse(
            SocialType socialType, String providerId);

}