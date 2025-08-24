package com.hanium.mom4u.global.crawling.listener;

import org.springframework.context.ApplicationEvent;

public class FinancialCrawlEvent extends ApplicationEvent {

    private String bucketName;

    public FinancialCrawlEvent(Object source, String bucketName) {
        super(source);
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
