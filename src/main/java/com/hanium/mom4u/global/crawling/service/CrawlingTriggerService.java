package com.hanium.mom4u.global.crawling.service;

import com.hanium.mom4u.global.crawling.event.CrawlEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlingTriggerService {

    private final ApplicationEventPublisher eventPublisher;

    public void triggerCrawling() {
        CrawlEvent event = new CrawlEvent(this);
        eventPublisher.publishEvent(event);
    }
}
