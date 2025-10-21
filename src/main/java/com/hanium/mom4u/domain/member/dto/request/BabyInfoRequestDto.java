package com.hanium.mom4u.domain.member.dto.request;

import com.hanium.mom4u.domain.member.common.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(description = "태아 정보 요청 DTO")
public class BabyInfoRequestDto {
    @Schema(description = "태아 ID")
    private long babyId;
    @Schema(description = "태아 이름")
    private String name;
    @Schema(description = "태아 성별")
    private Gender babyGender;
}
