package com.hanium.mom4u.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
@Getter
public class ProfileEditRequest {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "마지막 생리 시작일")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lmpDate;

    @Schema(description = "프로필 이미지 URL")
    private String imgUrl;
}
