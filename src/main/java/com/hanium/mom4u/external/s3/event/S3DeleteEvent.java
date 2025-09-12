package com.hanium.mom4u.external.s3.event;

import org.springframework.context.ApplicationEvent;

public class S3DeleteEvent extends ApplicationEvent {

    private final String objectKey;

    public S3DeleteEvent(Object source, String objectKey) {
        super(source);
        this.objectKey = objectKey;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
