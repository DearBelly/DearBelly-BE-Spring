package com.hanium.mom4u.domain.member.dto.response;

import com.hanium.mom4u.domain.member.common.BabyGender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BabyInfoResponseDto {

    private String name;
    @Schema(description = "출산 예정일")
    private LocalDate pregnantDate;
    private BabyGender babyGender;
    @Schema(description = "주차 수")
    private int weekLevel;
}
