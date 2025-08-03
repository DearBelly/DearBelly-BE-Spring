package com.hanium.mom4u.domain.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsImportScheduler {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Scheduled(cron = "0 */5 * * * *")
    public void triggerImport() {
        log.info("S3 Json에 대한 import 스케줄러 시작");
        applicationEventPublisher.publishEvent(new S3JsonImportEvent(this, bucketName));
    }
}
