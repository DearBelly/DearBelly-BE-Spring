package com.hanium.mom4u.domain.scan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "스캔 응답 DTO")
public class ScanResponseDto {

    private String pillName;
    private int isSafe; // 긍정이면 1, 부정이면 0
    private String description; // 약품에 대한 설명
}
