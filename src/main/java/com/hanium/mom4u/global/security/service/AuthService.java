package com.hanium.mom4u.global.security.service;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;

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

    @Transactional
    public String reissue(HttpServletRequest request, HttpServletResponse response
    ) {
        Long memberId = refreshTokenUtil.getMemberIdFromCookie(request);

        // 1. Redis에 저장된 refreshToken과 일치하는지 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        String savedToken = refreshTokenUtil.getRefreshToken(member.getId());
        if (!refreshTokenUtil.getRefreshTokenCookie(request).equals(savedToken)) {
            throw GeneralException.of(StatusCode.TOKEN_INVALID);
        }

        // 2. 새 token 발급
        refreshTokenUtil.removeRefreshTokenCookie(response);
        refreshTokenUtil.deleteRefreshToken(memberId);
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), Role.ROLE_USER);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenUtil.saveRefreshToken(memberId, newRefreshToken);
        refreshTokenUtil.addRefreshTokenCookie(response, newRefreshToken);

        return newAccessToken;
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        Long memberId = refreshTokenUtil.getMemberIdFromCookie(request);
        refreshTokenUtil.deleteRefreshToken(memberId);
        refreshTokenUtil.removeRefreshTokenCookie(response);
    }


    @Transactional
    public void withdraw(HttpServletRequest request, HttpServletResponse response) {
        Long memberId = refreshTokenUtil.getMemberIdFromCookie(request);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(StatusCode.MEMBER_NOT_FOUND));

        member.inactive();  // Soft delete 처리

        refreshTokenUtil.deleteRefreshToken(memberId);
        refreshTokenUtil.removeRefreshTokenCookie(response);
    }

}
