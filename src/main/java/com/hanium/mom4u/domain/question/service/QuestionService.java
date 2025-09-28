package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final DailyQuestionRepository dailyQuestionRepository; // JpaRepository + Custom

    /** (전역) 오늘 질문이 없으면 생성 */
    public void ensureTodayGlobalQuestion() {
        LocalDate today = LocalDate.now(KST);

        //  QueryDSL Custom 메서드 사용
        if (dailyQuestionRepository.existsGlobalOn(today)) return;

        // 어제 전역 질문(중복 방지용 텍스트)
        String yesterday = dailyQuestionRepository.findOneGlobalOn(today.minusDays(1))
                .map(DailyQuestion::getQuestionText)
                .orElse(null);

        var pick = dailyQuestionRepository.pickRandomExcluding(yesterday)
                .or(() -> dailyQuestionRepository.pickAny())
                .orElseThrow(() -> new IllegalStateException("No questions found"));

        DailyQuestion created = new DailyQuestion();
        created.setFamily(null);
        created.setQuestionText(pick.getContent());
        created.setQuestionId(pick.getId());
        dailyQuestionRepository.save(created);
    }

    /** 날짜/가족 기준으로 질문 1건(가족 우선 → 없으면 전역) */
    @Transactional(readOnly = true)
    public DailyQuestion getFor(LocalDate date, Long familyIdOrNull) {
        if (familyIdOrNull != null) {
            return dailyQuestionRepository.findOneForDate(familyIdOrNull, date)
                    .or(() -> dailyQuestionRepository.findOneGlobalOn(date))
                    .orElse(null);
        }
        return dailyQuestionRepository.findOneGlobalOn(date).orElse(null);
    }

    /** 질문 텍스트만 필요할 때 */
    @Transactional(readOnly = true)
    public String getTextFor(LocalDate date, Long familyIdOrNull) {
        DailyQuestion dq = getFor(date, familyIdOrNull);
        return (dq == null) ? null : dq.getQuestionText();
    }

    /** 매일 0시 전역 질문 보장 */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleEnsureToday() {
        ensureTodayGlobalQuestion();
    }
}
