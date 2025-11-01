package com.hanium.mom4u.domain.letter.repository;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.letter.entity.Letter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

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

    public Family newFamily() {
        Family f = newInstance(Family.class);
        em.persist(f);
        return f;
    }

    public Member newMember(Family fam, String nickname, String providerId) {
        Member m = newInstance(Member.class);
        set(m, "nickname", nickname);
        set(m, "providerId", providerId);
        if (fam != null) set(m, "family", fam);
        em.persist(m);
        return m;
    }

    public Letter newLetter(Member writer, Family family, String content, LocalDateTime createdAt) {
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
        Member me = newMember(fam, "me", "testProvider");

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = start.plusDays(1).minusNanos(1);

        newLetter(me, fam, "hello", start.plusHours(9));
        em.flush(); em.clear();

        boolean exists = letterRepository.existsByWriter_IdAndCreatedAtBetween(me.getId(), start, end);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc: 편지 조회")
    void findTopByWriter() {
        // given
        Family fam = newFamily();
        Member a = newMember(fam, "a", "a");
        Member b = newMember(fam, "b", "b");

        LocalDateTime d1 = LocalDateTime.of(2025, 9, 1, 10, 0);
        LocalDateTime d2 = LocalDateTime.of(2025, 9, 15, 10, 0);
        LocalDateTime d3 = LocalDateTime.of(2025, 10, 1, 10, 0);

        newLetter(a, fam, "9/1", d1);
        newLetter(b, fam, "9/15", d2);
        Letter expected = newLetter(a, fam, "10/1", d3);

        em.flush(); em.clear();

        // when
        Optional<Letter> result = letterRepository
                .findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        a.getId(),
                        d1,
                        d3.plusSeconds(1)
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expected.getId());
        assertThat(result.get().getContent()).isEqualTo("10/1");
    }

    @Test
    @DisplayName("findLetters: 해당 회원의 전체 편지 조회")
    void findLetters() {
        Family fam = newFamily();

        Member a = newMember(fam, "a", "a");
        Member b = newMember(fam, "b", "b");

        YearMonth ym = YearMonth.of(2025, 9);

        LocalDateTime d1 = LocalDateTime.of(2025, 9, 1, 10, 0);
        LocalDateTime d2 = LocalDateTime.of(2025, 9, 15, 10, 0);
        LocalDateTime d3 = LocalDateTime.of(2025, 10, 1, 10, 0);

        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end   = ym.plusMonths(1).atDay(1).atStartOfDay();

        newLetter(a, fam, "9/1", d1);
        newLetter(b, fam, "9/15", d2);
        newLetter(a, fam, "10/1", d3);
        LocalDateTime cursor = null;
        Pageable pageable = PageRequest.of(0, 10);
        em.flush(); em.clear();

        var list = letterRepository.findLetters(a.getId(), fam.getId(), start, end, cursor, pageable, true);

        assertThat(list).extracting("content").containsExactly("9/15", "9/1"); // desc 정렬
    }
}
