package com.hanium.mom4u.external.s3.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;

public class FileUploadEvent extends ApplicationEvent {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.s3.secret-key}")
    private String secretKey;

    private MultipartFile file;

    public FileUploadEvent(Object source, MultipartFile file) {
        super(source);
        this.bucketName = bucketName;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.file = file;
    }

    public FileUploadEvent(Object source, Clock clock) {
        super(source, clock);
    }

    public MultipartFile getFile() {
        return file;
    }

    public String getBucketName() {
        return bucketName;
    }
}
