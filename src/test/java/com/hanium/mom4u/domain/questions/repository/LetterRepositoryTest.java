package com.hanium.mom4u.domain.questions.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.question.entity.Letter;
import com.hanium.mom4u.domain.question.repository.LetterRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import(LetterRepositoryTest.QuerydslTestConfig.class)
class LetterRepositoryTest {

    @Autowired EntityManager em;
    @Autowired
    LetterRepository letterRepository;

    @TestConfiguration
    static class QuerydslTestConfig {
        @Bean JPAQueryFactory jpaQueryFactory(EntityManager em) { return new JPAQueryFactory(em); }
    }

    // ====== 리플렉션 헬퍼 ======
    private static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    private static void set(Object target, String field, Object value) {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(field); // 부모까지 순회
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException(new NoSuchFieldException(field));
    }

    private Family newFamily() {
        Family f = newInstance(Family.class);
        em.persist(f);
        return f;
    }

    private Member newMember(Family fam, String nickname, boolean seen) {
        Member m = newInstance(Member.class);
        set(m, "nickname", nickname);
        set(m, "hasSeenFamilyLetters", seen);
        if (fam != null) set(m, "family", fam);
        em.persist(m);
        return m;
    }

    private Letter newLetter(Member writer, Family family, String content, LocalDateTime createdAt) {
        Letter l = Letter.builder()
                .content(content)
                .writer(writer)
                .family(family)
                .build();
        set(l, "createdAt", createdAt);
        em.persist(l);
        return l;
    }

    @Test
    @DisplayName("existsByWriter_IdAndCreatedAtBetween: 오늘 쓴 편지 존재 여부")
    void existsToday() {
        Family fam = newFamily();
        Member me = newMember(fam, "me", true);

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = start.plusDays(1).minusNanos(1);

        newLetter(me, fam, "hello", start.plusHours(9));
        em.flush(); em.clear();

        boolean exists = letterRepository.existsByWriter_IdAndCreatedAtBetween(me.getId(), start, end);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByFamilyAndCreatedAtBetween: 가족의 월간 편지 조회 (최신순)")
    void findByFamilyAndRange() {
        Family fam = newFamily();

        Member a = newMember(fam, "a", true);
        Member b = newMember(fam, "b", true);

        LocalDateTime d1 = LocalDateTime.of(2025, 9, 1, 10, 0);
        LocalDateTime d2 = LocalDateTime.of(2025, 9, 15, 10, 0);
        LocalDateTime d3 = LocalDateTime.of(2025, 10, 1, 10, 0);

        newLetter(a, fam, "9/1", d1);
        newLetter(b, fam, "9/15", d2);
        newLetter(a, fam, "10/1", d3);

        em.flush(); em.clear();

        var list = letterRepository.findByFamilyAndCreatedAtBetween(
                fam,
                LocalDateTime.of(2025, 9, 1, 0, 0),
                LocalDateTime.of(2025, 9, 30, 23, 59, 59)
        );

        assertThat(list).extracting("content").containsExactly("9/15", "9/1"); // desc 정렬
    }


    @Test
    @DisplayName("findFeedForUser: 내 개인+가족 편지 섞여 최신순 페이징")
    void feedQuery() {
        Family fam = newFamily();
        Member me = newMember(fam, "me", true);
        Member other = newMember(fam, "other", true);

        newLetter(other, fam, "fam1", LocalDateTime.of(2025, 9, 20, 10, 0));
        newLetter(me, fam, "fam2", LocalDateTime.of(2025, 9, 21, 10, 0));
        newLetter(me, null, "mine-only", LocalDateTime.of(2025, 9, 22, 10, 0));

        em.flush(); em.clear();

        var page = org.springframework.data.domain.PageRequest.of(0, 10);
        var list = letterRepository.findFeedForUser(me.getId(), fam.getId(), null, page);

        assertThat(list).extracting("content")
                .containsExactly("mine-only", "fam2", "fam1");
    }
}
