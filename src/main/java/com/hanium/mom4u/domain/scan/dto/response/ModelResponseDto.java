package com.hanium.mom4u.domain.scan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "CV 모델 분석 결과 DTO")
public class ModelResponseDto {
    @Schema(description = "JOB의 correlationID")
    private String correlationId;
    @Schema(description = "약 이름")
    private String pillName;
    @Schema(description = "적용 가능 여부")
    private int isSafe; // 긍정이면 1, 부정이면 0
    @Schema(description = "약품에 대한 설명")
    private String description; // 약품에 대한 설명
    @Schema(description = "Message의 종료")
    private String finishedAt;  // ISO-8601
}
