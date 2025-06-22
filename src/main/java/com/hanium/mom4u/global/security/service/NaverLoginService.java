package com.hanium.mom4u.global.security.service;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.security.dto.response.LoginResponseDto;
import com.hanium.mom4u.global.security.dto.response.NaverProfileResponseDto;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import com.hanium.mom4u.global.util.NaverUtil;
import com.hanium.mom4u.global.util.RefreshTokenUtil;
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
        NaverProfileResponseDto profile = naverUtil.findProfile(naverUtil.getToken(code, state).getAccessToken());
        Member member = memberRepository.findByEmailAndSocialType(profile.getResponse().email, SocialType.NAVER)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .name(profile.getResponse().getName())
                            .nickname(profile.getResponse().getNickname())
                            .email(profile.getResponse().getEmail())
                            .role(Role.ROLE_USER)
                            .socialType(SocialType.NAVER)
                            .gender("F".equals(profile.getResponse().getGender())
                                    ? "female"
                                    : ("M".equals(profile.getResponse().getGender())
                                    ? "male"
                                    : "unknown"))
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
