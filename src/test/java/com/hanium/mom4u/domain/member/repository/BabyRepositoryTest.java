package com.hanium.mom4u.domain.member.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ActiveProfiles("test")
@DataJpaTest
@EnableJpaRepositories(basePackageClasses = BabyRepository.class)
class BabyRepositoryTest {

    @Autowired
    private BabyRepository babyRepository;

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


    @Test
    @DisplayName("부모 삭제 시 태아정보 삭제")
    void 부모_삭제_시_태아정보_삭제() {
        // given
        Baby baby1 = new Baby(null, false, Gender.MALE, "testBaby1", 2025, testMember1);
        Baby baby2 = new Baby(null, false, Gender.FEMALE, "testBaby2", 2025, testMember1);
        Baby baby3 = new Baby(null, false, Gender.UNKNOWN, "testBaby3", 2025, testMember2);

        babyRepository.saveAll(List.of(baby1, baby2, baby3));
        entityManager.flush();
        entityManager.clear();

        // when
        babyRepository.deleteByMember(testMember1);
        entityManager.flush();
        entityManager.clear();

        // then
        List<Baby> remainingBabies = babyRepository.findAll();
        assertThat(remainingBabies).hasSize(1);
        assertThat(remainingBabies.get(0).getName()).isEqualTo("testBaby3");
        assertThat(remainingBabies.get(0).getMember().getId()).isEqualTo(testMember2.getId());
    }

    @Test
    @DisplayName("등록된 태아정보가 없을 때 빈 리스트 성공")
    void 등록된_태아정보가_없을_때_성공() {
        // given
        Baby baby1 = new Baby(null, true, Gender.MALE, "baby1", 2025, testMember1); // 종료됨
        babyRepository.save(baby1);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Baby> result = babyRepository.findOngoingByMemberId(testMember2.getId(), PageRequest.of(0, 1));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("가족단위 등록태아 조회 성공")
    void 가족단위_등록태아_조회_성공() {
        // given
        Baby baby1 = new Baby(null, false, Gender.MALE, "testBaby1", 2025, testMember1);
        babyRepository.save(baby1);
        babyRepository.flush();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Baby baby2 = new Baby(null, false, Gender.FEMALE, "testBaby2", 2025, testMember2);
        babyRepository.save(baby2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Baby> result = babyRepository.findOngoingByFamilyId(
                testFamily.getId(),
                PageRequest.of(0, 1)
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("testBaby1");
    }

    @Test
    @DisplayName("가족단위 등록된 태아정보가 없을 때 빈 리스트 성공")
    void 가족단위_등록된_태아정보가_없을때_빈리스트_성공() {
        // given
        Baby baby1 = new Baby(null, true, Gender.MALE, "testBaby1", 2025, testMember1);
        babyRepository.save(baby1);
        babyRepository.flush();

        // when
        List<Baby> result = babyRepository.findOngoingByFamilyId(
                testFamily.getId(),
                PageRequest.of(0, 1)
        );

        // then
        assertThat(result).isEmpty();
    }
}