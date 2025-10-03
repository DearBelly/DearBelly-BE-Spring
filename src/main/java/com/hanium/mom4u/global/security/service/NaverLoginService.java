package com.hanium.mom4u.global.security.service;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.security.dto.response.LoginResponseDto;
import com.hanium.mom4u.global.security.dto.response.NaverProfileResponseDto;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import com.hanium.mom4u.global.security.util.NaverUtil;
import com.hanium.mom4u.global.security.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NaverLoginService {

    private final NaverUtil naverUtil;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenUtil refreshTokenUtil;

    public String getCode() {
        return naverUtil.buildLoginUrl();
    }

    @Transactional
    public LoginResponseDto loginWithNaver(HttpServletResponse response, String code, String state) {
        var token    = naverUtil.getToken(code, state);
        NaverProfileResponseDto profile = naverUtil.findProfile(token.getAccessToken());

        String providerId = profile.getResponse().getId();
        String email      = profile.getResponse().getEmail();
        String name       = profile.getResponse().getName();
        String nickname   = profile.getResponse().getNickname();

        // gender 매핑
        Gender mappedGender = mapNaverGender(profile.getResponse().getGender());

        //  활성 회원만 조회 → 없으면 신규 생성(탈퇴 이력은 무시)
        var found = memberRepository.findBySocialTypeAndProviderIdAndIsInactiveFalse(SocialType.NAVER, providerId);
        boolean isNew = found.isEmpty();

        Member member = found.orElseGet(() -> {
            Member m = Member.builder()
                    .socialType(SocialType.NAVER)
                    .providerId(providerId)   // 핵심 식별자
                    .email(email)
                    .name(name)
                    .nickname(nickname)
                    .gender(mappedGender)
                    .role(Role.ROLE_USER)
                    .isInactive(false)
                    .build();
            try {
                return memberRepository.save(m);
            } catch (DataIntegrityViolationException e) {
                // 동시 가입 시 유니크 충돌 대비 멱등 처리
                return memberRepository.findBySocialTypeAndProviderIdAndIsInactiveFalse(SocialType.NAVER, providerId)
                        .orElseThrow();
            }
        });

        // (선택) 기존 회원의 gender가 비어있으면 보정
        if (member.getGender() == null) {
            member.setGender(mappedGender);
        }

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

    private Gender mapNaverGender(String g) {
        if ("F".equalsIgnoreCase(g)) return Gender.FEMALE;
        if ("M".equalsIgnoreCase(g)) return Gender.MALE;
        return Gender.UNKNOWN; // 값 없음/기타
    }
}
