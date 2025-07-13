package com.hanium.mom4u.global.security.dto.response;

public class KakaoLoginResult {
    private final String accessToken;
    private final String refreshToken;
    private final LoginResponseDto userInfo;

    public KakaoLoginResult(String accessToken, String refreshToken, LoginResponseDto userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userInfo = userInfo;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public LoginResponseDto getUserInfo() { return userInfo; }
}

