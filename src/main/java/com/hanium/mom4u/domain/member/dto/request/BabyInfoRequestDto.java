package com.hanium.mom4u.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hanium.mom4u.domain.member.common.Gender;
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
    @Schema(description = "마지막 생리 시작일(LMP)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate lmpDate;
    @Schema(description = "태아 성별")
    private Gender babyGender;

}
