package com.hanium.mom4u.global.security.service;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.security.dto.response.GoogleProfileResponseDto;
import com.hanium.mom4u.global.security.dto.response.LoginResponseDto;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import com.hanium.mom4u.global.security.util.GoogleUtil;
import com.hanium.mom4u.global.security.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        String providerId = profile.getId();
        String email      = profile.getEmail();
        String name       = profile.getName();
        String nickname   = profile.getGivenName();

        var found = memberRepository.findBySocialTypeAndProviderIdAndIsInactiveFalse(
                SocialType.GOOGLE, providerId);
        boolean isNew = found.isEmpty();

        Member member = found.orElseGet(() -> {
            Member m = Member.builder()
                    .socialType(SocialType.GOOGLE)
                    .providerId(providerId)
                    .email(email)
                    .name(name)
                    .nickname(nickname)
                    .role(Role.ROLE_USER)
                    .isInactive(false)
                    .build();
            try {
                return memberRepository.save(m);
            } catch (DataIntegrityViolationException e) {
                // 동시 가입 시도 등 유니크 충돌 대비 멱등 처리
                return memberRepository.findBySocialTypeAndProviderIdAndIsInactiveFalse(
                        SocialType.GOOGLE, providerId).orElseThrow();
            }
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
                .isNew(isNew)
                .build();
    }

}
