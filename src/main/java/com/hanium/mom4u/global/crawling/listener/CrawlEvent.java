package com.hanium.mom4u.global.crawling.listener;

import org.springframework.context.ApplicationEvent;

public class CrawlEvent extends ApplicationEvent {

    public CrawlEvent(Object source) {
        super(source);
    }
}
