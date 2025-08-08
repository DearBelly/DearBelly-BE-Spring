package com.hanium.mom4u.domain.news.listener;

import org.springframework.context.ApplicationEvent;

public class S3JsonImportEvent extends ApplicationEvent {

    private final String bucketName;

    public S3JsonImportEvent(Object source, String bucketName) {
        super(source);
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
