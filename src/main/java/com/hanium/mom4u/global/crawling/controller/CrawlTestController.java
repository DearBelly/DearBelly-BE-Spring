package com.hanium.mom4u.global.crawling.controller;

import com.hanium.mom4u.domain.news.service.S3JsonImportEvent;
import com.hanium.mom4u.external.s3.FileStorageService;
import com.hanium.mom4u.global.crawling.service.CrawlingTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/crawl")
public class CrawlTestController {

    private final CrawlingTriggerService crawlingTriggerService;

    private final FileStorageService fileStorageService;

    private final ApplicationEventPublisher eventPublisher;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;


    @PostMapping
    public ResponseEntity<String> startCrawling() {
        crawlingTriggerService.triggerCrawling();
        return ResponseEntity.ok("크롤링 요청 완료");
    }

    @PostMapping("/s3")
    public ResponseEntity<String> startImporting() {
        eventPublisher.publishEvent(new S3JsonImportEvent(this, bucketName));
        return ResponseEntity.ok("DB에 저장 완료");
    }
}
