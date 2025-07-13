package com.hanium.mom4u.global.security.service;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.security.dto.response.GoogleProfileResponseDto;
import com.hanium.mom4u.global.security.dto.response.LoginResponseDto;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import com.hanium.mom4u.global.util.GoogleUtil;
import com.hanium.mom4u.global.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleLoginService {

    private final MemberRepository memberRepository;
    private final GoogleUtil googleUtil;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenUtil refreshTokenUtil;

    // 인증 코드 받기
    public String getCode() {
        return googleUtil.buildLoginUrl();
    }

    @Transactional
    public LoginResponseDto loginWithGoogle(HttpServletResponse response, String code) {
        GoogleProfileResponseDto profile = googleUtil.findProfile(
                googleUtil.getToken(code).getAccessToken())
                ;
        Member member = memberRepository.findByEmailAndSocialTypeAndIsInactiveFalse(
                        profile.getEmail(), SocialType.GOOGLE)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .name(profile.getName())
                            .nickname(profile.getGivenName())
                            .email(profile.getEmail())
                            .role(Role.ROLE_USER)
                            .socialType(SocialType.GOOGLE)
                            .build();
                    return memberRepository.save(newMember);
                });


        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenUtil.saveRefreshToken(member.getId(), refreshToken);
        refreshTokenUtil.addRefreshTokenCookie(response, refreshToken);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .email(member.getEmail())
                .nickname(member.getNickname())
                .socialType(member.getSocialType())
                .build();
    }

}
