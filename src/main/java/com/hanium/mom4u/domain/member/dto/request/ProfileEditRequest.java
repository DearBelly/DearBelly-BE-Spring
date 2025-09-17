package com.hanium.mom4u.domain.member.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
@Getter
public class ProfileEditRequest {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "마지막 생리 시작일")
    private LocalDate lmpDate;

}
