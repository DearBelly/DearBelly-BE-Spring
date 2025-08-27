package com.hanium.mom4u.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hanium.mom4u.domain.member.common.Gender;
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
    private Gender babyGender;

    @Schema(description = "마지막 생리 시작일(LMP)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate lmpDate;

    @Schema(description = "임신 주차(오늘 기준, 0주차부터)")
    private int currentWeek;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    @Schema(description = "출산 예정일(계산값, LMP + 40주)")
    private LocalDate dueDateCalculated;
}
