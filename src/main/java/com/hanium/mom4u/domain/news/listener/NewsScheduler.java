package com.hanium.mom4u.domain.news.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsScheduler {
    @Value("${spring.cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 4 * * *") // 새벽 4시에 실행
    public void triggerEvent() {
        log.info("Event started");
        eventPublisher.publishEvent(new S3JsonImportEvent(this, BUCKET_NAME));
    }
}
