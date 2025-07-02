package com.hanium.mom4u.domain.member.dto.request;

import com.hanium.mom4u.domain.member.common.BabyGender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "태아 정보 요청 DTO")
public class BabyInfoRequestDto {
    @Schema(description = "태아 ID")
    private long babyId;
    @Schema(description = "태아 이름")
    private String name;
    @Schema(description = "출산 예정일")
    private LocalDate pregnantDate;
    @Schema(description = "태아 성별")
    private BabyGender babyGender;
    @Schema(description = "주차 수")
    private int weekLevel;
}
