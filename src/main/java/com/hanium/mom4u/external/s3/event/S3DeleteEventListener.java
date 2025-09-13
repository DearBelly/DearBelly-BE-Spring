package com.hanium.mom4u.external.s3.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3DeleteEventListener {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    @EventListener
    @Async("asyncExecutor")
    public void onS3DeleteEvent(S3DeleteEvent event) {

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(event.getObjectKey())
                .build();

        log.info("Deleting object {}", deleteRequest);
        s3Client.deleteObject(deleteRequest);
    }
}
