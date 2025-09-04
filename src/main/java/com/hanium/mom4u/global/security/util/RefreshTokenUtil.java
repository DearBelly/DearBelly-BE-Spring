package com.hanium.mom4u.global.security.util;

import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUtil {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    // Redis 서버에 저장
    public void saveRefreshToken(Long memberId, String refreshToken) {
        String key = "refreshToken:" + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.SECONDS);
    }

    // Redis 서버에서 조회
    public String getRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get("refreshToken:" + memberId);
    }

    // Redis 서버에서 삭제
    public void deleteRefreshToken(Long memberId) {
        redisTemplate.delete("refreshToken:" + memberId);
    }

    // 쿠키에 추가
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) refreshTokenExpiration);
        response.addCookie(cookie);
    }

    // 쿠키로부터 refreshToken 추출
    public String getRefreshTokenCookie(HttpServletRequest request) {

        log.info("쿠키로부터 Refresh Token 찾기 시작...");
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            log.warn("쿠키가 비어 있습니다.");
            throw GeneralException.of(StatusCode.EMPTY_COOKIE);
        }

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                String refreshToken = cookie.getValue();
                log.info("Refresh Token 발견. 유효성 검사 시작...");
                return refreshToken;
            }
        }
        throw GeneralException.of(StatusCode.REFRESH_NOT_FOUND);
    }

    // 쿠키로부터 ID 조회
    public Long getMemberIdFromCookie(HttpServletRequest request) {

        String refreshToken = getRefreshTokenCookie(request);

        if (jwtTokenProvider.validateToken(refreshToken)) {
            Long memberId = Long.parseLong(jwtTokenProvider.getMemberId(refreshToken));
            log.info("유효한 Refresh token입니다. Member Id: {}", memberId);
            return memberId;
        } else {
            log.warn("유효하지 않은 Refresh Token입니다.");
            throw GeneralException.of(StatusCode.INVALID_JWT_TOKEN);
        }
    }

    // 쿠키 삭제
    public void removeRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
