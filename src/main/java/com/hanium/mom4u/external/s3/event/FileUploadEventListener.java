package com.hanium.mom4u.external.s3.event;

import com.hanium.mom4u.global.redis.message.ImageJobMessage;
import com.hanium.mom4u.global.redis.publisher.ImageJobPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;


@RequiredArgsConstructor
@Component
public class FileUploadEventListener {

    private final S3Client s3Client;

    private final ImageJobPublisher imageJobPublisher;

    @Value("${app.redis.resultQueue:image-result-queue}")
    private String resultQueue;

    // (선택) HTTP 콜백 URL을 쓰고 싶을 때 설정
    @Value("${app.callback.url:}")
    private String callbackUrl;


    /**
     * S3에 받은 이미지를 업로드
     * @param event
     */
    @EventListener
    @Transactional
    public void handleFileUploadEvent(final FileUploadEvent event) throws IOException {
        MultipartFile imgFile = event.getFile();
        Path tempFile = Files.createTempFile("upload-", imgFile.getOriginalFilename());

        String key = "uploads/" + imgFile.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(event.getBucketName())
                .key(key)
                .contentType(imgFile.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, tempFile);

        String presignedUrl = "https://" + event.getBucketName() + ".s3.amazonaws.com/" + key;

        String correlationId = UUID.randomUUID().toString();

        // Redis Task 발행을 위한 Job 생성
        ImageJobMessage job = ImageJobMessage.builder()
                .correlationId(correlationId)
                .presignedUrl(presignedUrl)
                .replyQueue(resultQueue)
                .callbackUrl(callbackUrl.isBlank() ? null : callbackUrl)
                .createdAt(OffsetDateTime.now().toString())
                .ttlSec(600)
                .build();

        // Redis Queue로 발행
        imageJobPublisher.publish(job);
    }
}
