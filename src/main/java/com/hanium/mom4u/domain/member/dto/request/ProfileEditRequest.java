package com.hanium.mom4u.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
@Getter
public class ProfileEditRequest {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "출산 예정일")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @Schema(description = "프로필 이미지 URL")
    private String imgUrl;
}
