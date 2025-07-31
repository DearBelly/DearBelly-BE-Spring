package com.hanium.mom4u.global.crawling.event;

import org.springframework.context.ApplicationEvent;

public class CrawlEvent extends ApplicationEvent {

    public CrawlEvent(Object source) {
        super(source);
    }
}
