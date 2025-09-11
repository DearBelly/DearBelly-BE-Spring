package com.hanium.mom4u.external.s3.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Component
public class S3DeleteEventListener {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    private S3Client s3Client;

    @EventListener
    public void onS3DeleteEvent(S3DeleteEvent event) {

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(event.getObjectKey())
                .build();

        s3Client.deleteObject(deleteRequest);
        s3Client.close();
    }
}
