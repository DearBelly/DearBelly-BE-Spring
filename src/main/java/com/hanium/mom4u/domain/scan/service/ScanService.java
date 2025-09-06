package com.hanium.mom4u.domain.scan.service;

import com.hanium.mom4u.external.redis.common.RedisStreamNames;
import com.hanium.mom4u.external.redis.message.ImageJobMessage;
import com.hanium.mom4u.external.redis.publisher.ImageJobPublisher;
import com.hanium.mom4u.external.s3.service.FileStorageService;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScanService {

    private final ImageJobPublisher imageJobPublisher;
    private final FileStorageService fileStorageService;

    /**
     * 전달받은 사진을 S3에 업로드
     * @param file
     */
    public String sendImageToS3(MultipartFile file) throws IOException {
        if (!file.getOriginalFilename().endsWith("jpeg") && !file.getOriginalFilename().endsWith("png")) {
            log.error("유효하지 않은 파일 양식 확인...");
            throw GeneralException.of(StatusCode.NOT_ENOUGH_FORMAT);
        }

        // S3에 업로드 후 PreSingedUrl 받기
        String presignedUrl = fileStorageService.uploadFileAndGetPresignedUrl(file);

        // Task 발행 시작
        String correlationId = UUID.randomUUID().toString();

        ImageJobMessage job = ImageJobMessage.builder()
                .correlationId(correlationId)
                .presignedUrl(presignedUrl)
                .replyQueue(RedisStreamNames.RESULT_STREAM)
                .contentType(file.getContentType())
                .createdAt(OffsetDateTime.now().toString())
                .ttlSec(600)
                .build();
        log.info("{} task 발행..", correlationId);

        // Redis Queue로 발행
        imageJobPublisher.publish(job);

        return correlationId;
    }


}
