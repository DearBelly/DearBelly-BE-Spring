package com.hanium.mom4u.domain.scan.controller;

import com.hanium.mom4u.domain.scan.dto.response.ModelResponseDto;
import com.hanium.mom4u.domain.scan.registry.PendingSinkRegistry;
import com.hanium.mom4u.domain.scan.service.ScanService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.io.IOException;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
@Tag(name = "스캔 API Controller", description = "스캔 API Controller입니다.")
public class ScanController {

    private final ScanService scanService;
    private final PendingSinkRegistry registry;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "스캔 요청 API", description = """
            사진 파일을 업로드해주세요. png와 jpeg 확장자가 아니면 제외됩니다.
            """)
    public Mono<ResponseEntity<CommonResponse<ModelResponseDto>>> scan(
            @RequestPart("file") MultipartFile file) throws IOException {

        String correlationId = scanService.sendImageToS3(file);

        Sinks.One<ModelResponseDto> sink = registry.create(correlationId);

        return sink.asMono()
                .map(dto -> ResponseEntity.ok(CommonResponse.onSuccess(dto)))
                .timeout(Duration.ofMinutes(3)) // TODO : TimeOut 조정
                .onErrorResume(TimeoutException.class, te -> {
                    registry.completeTimeout(correlationId);
                    CommonResponse<ModelResponseDto> body = new CommonResponse<>(
                            false, HttpStatus.GATEWAY_TIMEOUT.value(),
                            "응답 지연으로 인해 종료되었습니다. : correlationId=" + correlationId,
                            null
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body));
                })
                .doFinally(sig -> registry.take(correlationId));
    }

}
