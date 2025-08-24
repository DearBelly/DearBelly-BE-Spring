package com.hanium.mom4u.global.crawling.listener;

import org.springframework.context.ApplicationEvent;

public class HealthCrawlEvent extends ApplicationEvent {

    private String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    public HealthCrawlEvent(Object source, String bucketName) {
        super(source);
        this.bucketName = bucketName;
    }
}
