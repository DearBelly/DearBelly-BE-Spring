package com.hanium.mom4u.domain.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionScheduler {

    private final QuestionService questionService;

    /** 매일 0시: 트리거(신호) */
    @Async("schedulerExecutor")
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleEnsureToday() {
        log.info("[SCHED] ensureTodayGlobalQuestion START");
        questionService.ensureTodayGlobalQuestionAsync(); // ★ 서비스 호출
    }
}
