package com.hanium.mom4u.global.security.dto.response;

import com.hanium.mom4u.domain.member.common.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String email;
    private String nickname;
    private SocialType socialType;
}
