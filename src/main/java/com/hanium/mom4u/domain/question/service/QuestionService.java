package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final DailyQuestionRepository dailyQuestionRepository;

    /**  매일 0시: 트리거(신호) */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleEnsureToday() {
        log.info("[SCHED] ensureTodayGlobalQuestion START");
        ensureTodayGlobalQuestionAsync();
    }

    @Transactional
    public void ensureTodayGlobalQuestion() {
        ensureTodayGlobalQuestionCore();
    }

    @Async("asyncExecutor")
    @Transactional
    public void ensureTodayGlobalQuestionAsync() {
        long t0 = System.nanoTime();
        try {
            ensureTodayGlobalQuestionCore();
        } catch (Exception e) {
            log.error("[SCHED] ensureTodayGlobalQuestion FAILED", e);
        } finally {
            long tookMs = (System.nanoTime() - t0) / 1_000_000;
            log.info("[SCHED] ensureTodayGlobalQuestion END took={}ms", tookMs);
        }
    }

    /**전역 ‘오늘 질문’ 없으면 생성 */
    void ensureTodayGlobalQuestionCore() {
        LocalDate today = LocalDate.now(KST);

        if (dailyQuestionRepository.existsGlobalOn(today)) {
            log.info("[QUESTION] already exists for date={}", today);
            return;
        }

        // 어제 전역 질문(중복 방지 텍스트)
        String yesterday = dailyQuestionRepository.findOneGlobalOn(today.minusDays(1))
                .map(DailyQuestion::getQuestionText)
                .orElse(null);

        var pick = dailyQuestionRepository.pickRandomExcluding(yesterday)
                .or(() -> dailyQuestionRepository.pickAny())
                .orElseThrow(() -> GeneralException.of(StatusCode.QUESTION_NOT_FOUND));

        DailyQuestion created = new DailyQuestion();
        created.setFamily(null); // 전역
        created.setQuestionText(pick.getContent());
        created.setQuestionId(pick.getId());
        dailyQuestionRepository.save(created);

        log.info("[QUESTION] created global date={} originId={} content='{}'",
                today, pick.getId(), pick.getContent());
    }

    /**  날짜/가족 기준 1건 조회 (가족 우선 → 없으면 전역) */
    @Transactional(readOnly = true)
    public DailyQuestion getFor(LocalDate date, Long familyIdOrNull) {
        if (familyIdOrNull != null) {
            return dailyQuestionRepository.findOneForDate(familyIdOrNull, date)
                    .or(() -> dailyQuestionRepository.findOneGlobalOn(date))
                    .orElse(null);
        }
        return dailyQuestionRepository.findOneGlobalOn(date).orElse(null);
    }

    /**  질문 ‘텍스트’만 필요할 때 */
    @Transactional(readOnly = true)
    public String getTextFor(LocalDate date, Long familyIdOrNull) {
        DailyQuestion dq = getFor(date, familyIdOrNull);
        return (dq == null) ? null : dq.getQuestionText();
    }
}
