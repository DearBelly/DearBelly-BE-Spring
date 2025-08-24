package com.hanium.mom4u.global.crawling.listener;

import com.hanium.mom4u.global.crawling.util.SeleniumHealthCrawler;
import com.hanium.mom4u.global.crawling.util.SeleniumPregnancyCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlEventListener {

    private final SeleniumHealthCrawler seleniumHealthCrawler;
    private final SeleniumPregnancyCrawler seleniumPregnancyCrawler;

    @Async
    @EventListener
    public void handleHealthCrawlEvent(HealthCrawlEvent event) {
        log.info("건강 정보 크롤링 이벤트 수신");
        seleniumHealthCrawler.crawl();
    }

    @Async
    @EventListener
    public void handleFinancialCrawlEvent(FinancialCrawlEvent event) {
        log.info("지원금 정보 크롤링 이벤트 수신");
        seleniumHealthCrawler.crawl();
    }

    @Async
    @EventListener
    public void handlePregnancyCrawlEvent(PregnancyCrawlEvent event) {
        log.info("임신준비 정보 크롤링 이벤트 수신");
        seleniumPregnancyCrawler.crawl();
    }
}
