package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionScheduler {

    private final QuestionService questionService;

    /** 매일 0시: 트리거(신호) */
    @Async("schedulerExecutor")
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleEnsureToday() {
        try {
            log.info("[SCHED] ensureTodayGlobalQuestion START");
            questionService.ensureTodayGlobalQuestion();
        } catch (Exception e) {
            log.error("[SCHED] Daily Question Scheduling FAILED", e);
            throw GeneralException.of(StatusCode.QUESTION_SCHEDULER_ERROR);
        }
    }
}
