package com.hanium.mom4u.global.crawling.controller;

import com.hanium.mom4u.global.crawling.service.CrawlingTriggerService;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/crawl")
@Tag(name = "크롤링 API Controller", description = "서버에서 크롤링 트리거로 사용하기 위한 임시 Controller입니다.(추후삭제)")
public class CrawlTestController {

    private final CrawlingTriggerService crawlingTriggerService;

    @PostMapping()
    public ResponseEntity<String> crawl(
            @RequestParam("order") int order
    ) {
        if (order == 1) {
            crawlingTriggerService.triggerHealthCrawling();
            return ResponseEntity.ok("HEALTH 크롤링 요청 완료");
        } else if (order == 2) {
            crawlingTriggerService.triggerFinancialCrawling();
            return ResponseEntity.ok("FINANCIAL 크롤링 요청 완료");
        } else if (order == 3) {
            crawlingTriggerService.triggerPregnancyCrawling();
            return ResponseEntity.ok("PREGNANCY_PLANNING 크롤링 요청 완료");
        } else {
            throw GeneralException.of(StatusCode.FAILURE_TEST);
        }
    }

    @PostMapping("/s3")
    public ResponseEntity<String> startImporting() {
        crawlingTriggerService.startImporting();
        return ResponseEntity.ok("DB에 저장 완료");
    }
}
