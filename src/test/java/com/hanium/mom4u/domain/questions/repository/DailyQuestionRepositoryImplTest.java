package com.hanium.mom4u.domain.questions.repository;

import com.hanium.mom4u.domain.question.entity.DailyQuestion;
import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.question.entity.Question;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import(DailyQuestionRepositoryImplTest.QuerydslTestConfig.class)
class DailyQuestionRepositoryImplTest {

    @Autowired EntityManager em;
    @Autowired DailyQuestionRepository dailyQuestionRepository;

    @TestConfiguration
    static class QuerydslTestConfig {
        @Bean JPAQueryFactory jpaQueryFactory(EntityManager em) { return new JPAQueryFactory(em); }
    }

    // ===== reflection helpers =====
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
                Field f = c.getDeclaredField(field);
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

    private Question newQuestion(String content) {
        Question q = newInstance(Question.class);
        set(q, "content", content);
        em.persist(q);
        em.flush();
        return q;
    }

    private Family newFamily() {
        Family f = newInstance(Family.class);
        em.persist(f);
        em.flush();
        return f;
    }

    /**
     * createdAt은 @PrePersist/감사로 now()가 들어갈 수 있으니,
     * 테스트에서는 굳이 세팅하지 않고 DB now 기반으로 검증합니다.
     */
    private DailyQuestion newDailyQuestion(Family fam, String text, Long questionId) {
        DailyQuestion dq = newInstance(DailyQuestion.class);
        set(dq, "questionText", text);
        set(dq, "family", fam);            // fam == null 이면 전역
        set(dq, "questionId", questionId);
        em.persist(dq);
        em.flush();
        return dq;
    }

    @Test
    @DisplayName("findOneForDate: 가족 우선, 없으면 전역")
    void findOneForDate_prefersFamilyThenGlobal() {
        LocalDate today = LocalDate.now();   // ★ 핵심: now 기준으로 조회
        Question q1 = newQuestion("전역질문");
        Question q2 = newQuestion("가족질문");
        Family fam = newFamily();

        // 전역 질문
        newDailyQuestion(null, "전역질문", q1.getId());
        // 같은 날 가족 질문(더 최신이라는 가정은 어차피 같은 날이므로 createdAt desc로 가족 건이 먼저면 OK)
        newDailyQuestion(fam, "가족질문", q2.getId());

        em.flush(); em.clear();

        var found = dailyQuestionRepository.findOneForDate(fam.getId(), today);
        assertThat(found).isPresent();
        assertThat(found.get().getQuestionText()).isEqualTo("가족질문");

        // 가족 id 없으면 전역으로
        var globalOnly = dailyQuestionRepository.findOneForDate(null, today);
        assertThat(globalOnly).isPresent();
        assertThat(globalOnly.get().getQuestionText()).isEqualTo("전역질문");
    }

    @Test
    @DisplayName("existsGlobalOn / findOneGlobalOn")
    void existsAndFindGlobal() {
        LocalDate today = LocalDate.now();   // ★ now 기준
        Question q = newQuestion("전역");
        newDailyQuestion(null, "전역", q.getId());
        em.flush(); em.clear();

        boolean exists = dailyQuestionRepository.existsGlobalOn(today);
        assertThat(exists).isTrue();

        var one = dailyQuestionRepository.findOneGlobalOn(today);
        assertThat(one).isPresent();
        assertThat(one.get().getQuestionText()).isEqualTo("전역");
    }

    @Test
    @DisplayName("pickRandomExcluding / pickAny")
    void pickRandom() {
        Question q1 = newQuestion("A");
        Question q2 = newQuestion("B");
        em.flush(); em.clear();

        var excludeA = dailyQuestionRepository.pickRandomExcluding("A");
        assertThat(excludeA).isPresent();
        assertThat(excludeA.get().getContent()).isEqualTo("B");

        var anyPick = dailyQuestionRepository.pickAny();
        assertThat(anyPick).isPresent();
        assertThat(anyPick.get().getContent()).isIn("A", "B");
    }
}
