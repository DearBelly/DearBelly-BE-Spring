package com.hanium.mom4u.domain.scan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "CV 모델 분석 결과 DTO")
public class ModelResponseDto {
    private String pillName;
    private String correlationId;
    private String label;
    private double confidence;
    private String finishedAt;  // ISO-8601
}
