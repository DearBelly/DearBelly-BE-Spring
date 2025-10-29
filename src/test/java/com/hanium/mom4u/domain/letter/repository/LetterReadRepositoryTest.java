package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.letter.entity.Letter;
import com.hanium.mom4u.domain.letter.entity.LetterRead;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.global.config.QueryDSLConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDSLConfig.class)
class LetterReadRepositoryTest {

    @Autowired JPAQueryFactory jpaQueryFactory;
    @Autowired EntityManager entityManager;

    private Letter testLetter;
    private Member testMember;
    private LetterRead testLetterRead;
    private Family testFamily;
    @Autowired
    private LetterReadRepository letterReadRepository;

    @BeforeEach
    void setUp() {
        testMember = new Member(null, "test1@naver.com", "test1", "test1", SocialType.NAVER, "1", Role.ROLE_USER, Gender.FEMALE, LocalDate.of(2025, 10, 10), true, LocalDate.of(2025, 10, 10), false);
        testFamily = new Family();
        testMember.setFamily(testFamily);
        testLetter = new Letter(null, "안녕하세요", testMember, testFamily);
        testLetterRead = new LetterRead(null, testLetter, testMember, null);
    }

    @Test
    @DisplayName("JPAQueryFactory 로드 테스트")
    void jpaQueryFactory_is_loaded() {
        assertThat(jpaQueryFactory).isNotNull();
    }

    @Test
    @DisplayName("회원이 편지읽은 상태로 수정")
    void 회원이_편지읽은_상태로_수정() {
        // null 일 경우가 읽지 않은 것임
        // given
        entityManager.persist(testMember);
        entityManager.persist(testFamily);
        entityManager.persist(testLetter);
        entityManager.persist(testLetterRead);
        entityManager.flush();
        entityManager.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        int result = letterReadRepository.markRead(1L, 1L, now); // 쿼리 실행 변화 개수
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(result).isEqualTo(1);
        LetterRead updated = letterReadRepository.findById(1L).orElseThrow();
        assertThat(updated.getReadAt()).isNotNull();
    }
}