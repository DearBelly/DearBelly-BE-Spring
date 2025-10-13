package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.question.entity.DailyQuestion;
import com.hanium.mom4u.domain.question.entity.Question;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final DailyQuestionRepository dailyQuestionRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @PersistenceContext
    private EntityManager em;

    /** 동기 호출용 (트랜잭션 적용) */
    @Transactional
    public void ensureTodayGlobalQuestion() {
        ensureTodayGlobalQuestionCore();
    }

    /** 비동기 호출용 (트랜잭션 적용 + 예외 처리) */
    @Transactional
    public void ensureTodayGlobalQuestionAsync() {
        long t0 = System.nanoTime();
        try {
            ensureTodayGlobalQuestionCore();
        } catch (Exception e) {
            log.error("[SCHED] ensureTodayGlobalQuestion FAILED", e);
            throw e; // ★ 트랜잭션 롤백을 위해 예외 재던지기
        } finally {
            long tookMs = (System.nanoTime() - t0) / 1_000_000;
            log.info("[SCHED] ensureTodayGlobalQuestion END took={}ms", tookMs);
        }
    }

    /** 전역 '오늘 질문' 없으면 생성 */
    private void ensureTodayGlobalQuestionCore() {
        LocalDate today = LocalDate.now(KST);

        if (dailyQuestionRepository.existsGlobalOn(today)) {
            log.info("[QUESTION] already exists for date={}", today);
            return;
        }

        // 어제 전역 질문(중복 방지 텍스트)
        String yesterday = dailyQuestionRepository.findOneGlobalOn(today.minusDays(1))
                .map(DailyQuestion::getQuestionText)
                .orElse(null);

        // 리턴 타입이 QuestionPick(프로젝션)
        var pick = dailyQuestionRepository.pickRandomExcluding(yesterday)
                .or(() -> dailyQuestionRepository.pickAny())
                .orElseThrow(() -> GeneralException.of(StatusCode.QUESTION_NOT_FOUND));

        // 엔티티 쿼리 없이 프록시로 연관관계 세팅
        Question qRef = em.getReference(Question.class, pick.getId());

        DailyQuestion created = new DailyQuestion();
        created.setFamily(null);                         // 전역
        created.setQuestion(qRef);                      // FK -> question_id
        created.setOriginQuestion(qRef);                // FK -> origin_question_id
        created.setDailyQuestionText(pick.getContent()); // 스냅샷 텍스트

        dailyQuestionRepository.save(created);

        log.info("[QUESTION] created global date={} originId={} content='{}'",
                today, pick.getId(), pick.getContent());
    }

    @Transactional(readOnly = true)
    public DailyQuestion getFor(LocalDate date, Long familyIdOrNull) {
        if (familyIdOrNull != null) {
            return dailyQuestionRepository.findOneForDate(familyIdOrNull, date)
                    .or(() -> dailyQuestionRepository.findOneGlobalOn(date))
                    .orElse(null);
        }
        return dailyQuestionRepository.findOneGlobalOn(date).orElse(null);
    }

    @Transactional(readOnly = true)
    public String getTextFor(LocalDate date, Long familyIdOrNull) {
        DailyQuestion dq = getFor(date, familyIdOrNull);
        return (dq == null) ? null : dq.getQuestionText();
    }
}
