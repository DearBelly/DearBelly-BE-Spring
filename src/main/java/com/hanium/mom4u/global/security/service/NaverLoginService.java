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
        NaverProfileResponseDto profile =
                naverUtil.findProfile(naverUtil.getToken(code, state).getAccessToken());

        // 네이버 gender: "M" | "F" | null
        String naverGender = profile.getResponse().getGender();
        Gender mappedGender = mapNaverGender(naverGender);

        Member member = memberRepository
                .findByEmailAndSocialTypeAndIsInactiveFalse(profile.getResponse().getEmail(), SocialType.NAVER)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .name(profile.getResponse().getName())
                            .nickname(profile.getResponse().getNickname())
                            .email(profile.getResponse().getEmail())
                            .role(Role.ROLE_USER)
                            .socialType(SocialType.NAVER)
                            .gender(mappedGender)              //  enum으로 저장
                            .build();
                    return memberRepository.save(newMember);
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
                .build();
    }

    private Gender mapNaverGender(String g) {
        if ("F".equalsIgnoreCase(g)) return Gender.FEMALE;
        if ("M".equalsIgnoreCase(g)) return Gender.MALE;
        return Gender.UNKNOWN; // 값 없음/기타
    }
}
