package com.hanium.mom4u.domain.scan.service;

import com.hanium.mom4u.domain.scan.dto.response.ModelResponseDto;
import com.hanium.mom4u.domain.scan.dto.response.ScanResponseDto;
import com.hanium.mom4u.external.s3.event.FileUploadEvent;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScanService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 전달받은 사진을 S3에 업로드
     * @param file
     */
    public void sendImageToS3(MultipartFile file) {
        if (!file.getOriginalFilename().endsWith("jpeg") || !file.getOriginalFilename().endsWith("png")) {
            log.error("유효하지 않은 파일 양식 확인...");
            throw GeneralException.of(StatusCode.NOT_ENOUGH_FORMAT);
        }

        // S3에 업로드하도록 이벤트 발행
        eventPublisher.publishEvent(new FileUploadEvent(this, file));

    }

    public ScanResponseDto processResult(ModelResponseDto dto) {
        return null;
    }
}
