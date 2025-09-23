package com.hanium.mom4u.global.security.service;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import com.hanium.mom4u.global.security.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenUtil refreshTokenUtil;
    private final MemberRepository memberRepository;
    private final AuthenticatedProvider authenticatedProvider;

    @Transactional
    public String reissue(HttpServletRequest request, HttpServletResponse response) {
        String rt = refreshTokenUtil.getRefreshTokenValue(request)
                .orElseThrow(() -> GeneralException.of(StatusCode.REFRESH_NOT_FOUND));

        Long memberId = refreshTokenUtil.getMemberIdFromCookieOptional(request)
                .orElseThrow(() -> GeneralException.of(StatusCode.TOKEN_INVALID));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        String saved = refreshTokenUtil.getRefreshToken(member.getId());
        if (saved == null || !rt.equals(saved)) {
            throw GeneralException.of(StatusCode.TOKEN_INVALID);
        }

        refreshTokenUtil.removeRefreshTokenCookie(response);
        refreshTokenUtil.deleteRefreshToken(memberId);

        String newAccessToken  = jwtTokenProvider.createAccessToken(member.getId(), Role.ROLE_USER);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenUtil.saveRefreshToken(memberId, newRefreshToken);
        refreshTokenUtil.addRefreshTokenCookie(response, newRefreshToken);

        return newAccessToken;
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenUtil.getMemberIdFromCookieOptional(request).ifPresentOrElse(id -> {
            log.info("[LOGOUT] deleting refresh in Redis for memberId={}", id);
            refreshTokenUtil.deleteRefreshToken(id);
        }, () -> log.info("[LOGOUT] no refresh cookie found; skip Redis delete"));

        refreshTokenUtil.removeRefreshTokenCookie(response);
    }


    @Transactional
    public void withdraw(HttpServletRequest request, HttpServletResponse response) {
        Long memberId = authenticatedProvider.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        member.inactive();

        refreshTokenUtil.deleteRefreshToken(memberId);
        refreshTokenUtil.removeRefreshTokenCookie(response);
    }
}
