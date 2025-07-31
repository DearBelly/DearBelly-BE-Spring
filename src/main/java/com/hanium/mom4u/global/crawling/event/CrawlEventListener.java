package com.hanium.mom4u.global.crawling.event;

import com.hanium.mom4u.global.util.SeleniumCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlEventListener {

    private final SeleniumCrawler seleniumCrawler;

    @Async
    @EventListener
    public void handleCrawlEvent(CrawlEvent event) {
        log.info("크롤링 이벤트 수신");
        seleniumCrawler.crawl();
    }
}
