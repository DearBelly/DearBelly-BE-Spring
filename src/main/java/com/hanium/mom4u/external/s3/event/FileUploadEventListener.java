package com.hanium.mom4u.external.s3.event;

import com.hanium.mom4u.external.s3.service.FileStorageService;
import com.hanium.mom4u.global.redis.common.RedisStreamNames;
import com.hanium.mom4u.global.redis.message.ImageJobMessage;
import com.hanium.mom4u.global.redis.publisher.ImageJobPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FileUploadEventListener {

    private final S3Client s3Client;

    private final ImageJobPublisher imageJobPublisher;

    private final static String S3_IMG_PATH = "scan/";
    private final FileStorageService fileStorageService;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // HTTP 콜백 URL을 쓰고 싶을 때 설정
    @Value("${app.callback.url:}")
    private String callbackUrl;


    /**
     * S3에 받은 이미지를 업로드
     * @param event
     */
    @EventListener
    @Transactional
    public void handleFileUploadEvent(final FileUploadEvent event) throws IOException {
        MultipartFile multipartFile = event.getFile();

        String name = multipartFile.getOriginalFilename() == null ? "image" : multipartFile.getOriginalFilename();

        String key = S3_IMG_PATH + UUID.randomUUID() + "_" + name;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(multipartFile.getContentType())
                .build();

        s3Client.putObject(
                request,
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                        multipartFile.getInputStream(), multipartFile.getSize()
                )
        );

        String presignedUrl = fileStorageService.generatePresignedPutUrl(key);
        log.info("업로드 및 presigned GET URL 발급 성공: {}", presignedUrl);
        String correlationId = UUID.randomUUID().toString();

        // Redis Task 발행을 위한 Job 생성
        ImageJobMessage job = ImageJobMessage.builder()
                .correlationId(correlationId)
                .presignedUrl(presignedUrl)
                .replyQueue(RedisStreamNames.JOB_STREAM)
                .callbackUrl(callbackUrl.isBlank() ? null : callbackUrl)
                .createdAt(OffsetDateTime.now().toString())
                .ttlSec(600)
                .build();
        log.info("{} task 발행..", correlationId);

        // Redis Queue로 발행
        imageJobPublisher.publish(job);
    }
}
