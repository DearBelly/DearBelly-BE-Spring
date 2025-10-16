package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ExtendWith(MockitoExtension.class)
class BabyServiceTest {

    @Mock
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private BabyRepository babyRepository;
    @InjectMocks
    private BabyService babyService;

    @Autowired
    private EntityManager entityManager;

    private Member testMember1;
    private Member testMember2;
    private Member testMember3;
    private Family testFamily;

    @BeforeEach
    void setUp() {
        testFamily = new Family();
        entityManager.persist(testFamily);

        testMember1 = new Member(null, "test1@naver.com", "test1", "test1", SocialType.NAVER, "1", Role.ROLE_USER, Gender.FEMALE, LocalDate.of(2025, 10, 10), true, LocalDate.of(2025, 10, 10), false);
        testMember2 = new Member(null, "test2@naver.com", "test2", "test2", SocialType.NAVER, "2", Role.ROLE_USER, Gender.FEMALE, LocalDate.of(2025, 10, 10), true, LocalDate.of(2025, 10, 10), false);
        testMember3 = new Member(null, "test3@naver.com", "test3", "test3", SocialType.NAVER, "3", Role.ROLE_USER, Gender.MALE, null, true, null, false);
        testMember1.setFamily(testFamily);
        testMember3.setFamily(testFamily);
        entityManager.persist(testMember1);
        entityManager.persist(testMember2);
        entityManager.persist(testMember3);

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        babyRepository.deleteAll();
    }

//    @Nested
//    class GetBabyInfo {
//
//
//        @Test
//        @DisplayName("태아 정보 등록 성공")
//        void 태아정보_등록_성공() {
//
//        }
//    }
}