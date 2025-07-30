package com.hanium.mom4u.global.crawling.controller;

import com.hanium.mom4u.global.crawling.service.CrawlingTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/crawl")
public class CrawlTestController {

    private final CrawlingTriggerService crawlingTriggerService;

    @PostMapping
    public ResponseEntity<String> startCrawling() {
        crawlingTriggerService.triggerCrawling();
        return ResponseEntity.ok("크롤링 요청 완료");
    }
}
