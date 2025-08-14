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
@Schema(description = "태아 정보 반환 DTO")
public class BabyInfoResponseDto {

    @Schema(description = "태아 ID")
    private long babyId;

    @Schema(description = "태아 이름")
    private String name;

    @Schema(description = "태아 성별")
    private BabyGender babyGender;

    @Schema(description = "마지막 생리 시작일(LMP)")
    private LocalDate lmpDate;

    @Schema(description = "임신 주차(오늘 기준, 0주차부터)")
    private int currentWeek;

    @Schema(description = "출산 예정일(계산값, LMP + 40주)")
    private LocalDate dueDateCalculated;
}
