package com.hanium.mom4u.global.crawling.service;

import com.hanium.mom4u.domain.news.listener.S3JsonImportEvent;
import com.hanium.mom4u.global.crawling.listener.FinancialCrawlEvent;
import com.hanium.mom4u.global.crawling.listener.HealthCrawlEvent;
import com.hanium.mom4u.global.crawling.listener.PregnancyCrawlEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 이벤트 발행을 위한 서비스 코드
 */
@Service
@RequiredArgsConstructor
public class CrawlingTriggerService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Health 정보 수집
     */
    public void triggerHealthCrawling() {
        HealthCrawlEvent event = new HealthCrawlEvent(this, bucketName);
        eventPublisher.publishEvent(event);
    }

    /**
     * Financial 정보 수집
     */
    public void triggerFinancialCrawling() {
        FinancialCrawlEvent event = new FinancialCrawlEvent(this, bucketName);
        eventPublisher.publishEvent(event);
    }

    /**
     * PregnancyPlanning 정보 수집
     */
    public void triggerPregnancyCrawling() {
        PregnancyCrawlEvent event = new PregnancyCrawlEvent(this, bucketName);
        eventPublisher.publishEvent(event);
    }

    public void startImporting() {
        eventPublisher.publishEvent(new S3JsonImportEvent(this, bucketName));
    }
}
