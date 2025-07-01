package com.hanium.mom4u.domain.member.dto.request;

import com.hanium.mom4u.domain.member.common.BabyGender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BabyInfoRequestDto {
    private String name;
    @Schema(description = "출산 예정일")
    private LocalDate pregnantDate;
    private BabyGender babyGender;
    @Schema(description = "주차 수")
    private int weekLevel;
}
