package com.hanium.mom4u.global.crawling.listener;

import org.springframework.context.ApplicationEvent;

public class PregnancyCrawlEvent extends ApplicationEvent {

    private String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    public PregnancyCrawlEvent(Object source, String bucketName) {
        super(source);
        this.bucketName = bucketName;
    }
}
