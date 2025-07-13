package com.hanium.mom4u.domain.member.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByFamily(Family family);

    Optional<Member> findByEmailAndSocialTypeAndIsInactiveFalse(String email, SocialType socialType);

    List<Member> findAllByIsInactiveTrueAndInactiveDateBefore(LocalDate date);


}