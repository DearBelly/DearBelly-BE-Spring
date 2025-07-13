package com.hanium.mom4u.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.dto.response.KakaoTokenDto;
import com.hanium.mom4u.global.security.dto.response.KakaoUserInfoResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

@Slf4j
@Component
public class KakaoUtil {

    @Value("${spring.security.kakao.client_id}")
    private String clientId;

    @Value("${spring.security.kakao.redirect_uri}")
    private String redirectUri;

    private static final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private static final String KAPI_USER_URL_HOST = "https://kapi.kakao.com";

    public String buildLoginUrl() {
        return String.format(
                "%s/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                KAUTH_TOKEN_URL_HOST, clientId, redirectUri
        );
    }

    public KakaoTokenDto requestAccessToken(String code) {
        WebClient webClient = WebClient.create(KAUTH_TOKEN_URL_HOST);
        // 코드 유효성 검증 추가
        if (code == null || code.isEmpty()) {
            throw new BusinessException(StatusCode.KAKAO_AUTH_CODE_INVALID);
        }

        System.out.println("[DEBUG] client_id: " + clientId);
        System.out.println("[DEBUG] redirect_uri: " + redirectUri);
        System.out.println("[DEBUG] code: " + code);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        try {
            return webClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(KakaoTokenDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            String errorCode = extractKakaoErrorCode(errorBody); // KakaoLoginService의 메서드와 동일
            throw mapKakaoErrorToBusinessException(errorCode); // BusinessException으로 변환
        }
    }


    public KakaoUserInfoResponseDto requestProfile(String accessToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl(KAPI_USER_URL_HOST)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();

        return webClient.get()
                .uri("/v2/user/me")
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();
    }

    // KakaoUtil에 extractKakaoErrorCode 메서드 추가
    public String extractKakaoErrorCode(String errorBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(errorBody);
            String errorCode = rootNode.path("error_code").asText();
            if (!errorCode.isEmpty()) return errorCode;

            errorCode = rootNode.path("error").asText();
            if (!errorCode.isEmpty()) return errorCode;

            String errorDescription = rootNode.path("error_description").asText();
            if (errorDescription.contains("invalid_grant")) return "KOE320";

            return "UNKNOWN_ERROR";
        } catch (IOException ex) {
            return "UNKNOWN_ERROR";
        }
    }

    // KakaoUtil에 mapKakaoErrorToGeneralException 메서드 추가
    public GeneralException mapKakaoErrorToBusinessException(String errorCode) {
        switch (errorCode) {
            case "KOE320":
            case "invalid_grant":
                return new GeneralException(StatusCode.KAKAO_AUTH_CODE_INVALID);
            case "KOE303":
            case "invalid_request":
                return new GeneralException(StatusCode.KAKAO_REDIRECT_URI_MISMATCH);
            case "KOE101":
            case "invalid_client":
                return new GeneralException(StatusCode.KAKAO_CLIENT_INVALID);
            case "KOE102":
                return new GeneralException(StatusCode.KAKAO_INVALID_REQUEST);
            case "KOE006":
                return new GeneralException(StatusCode.KAKAO_REDIRECT_URI_INVALID);
            default:
                log.error("카카오 알 수 없는 에러 코드: {}", errorCode);
                return new GeneralException(StatusCode.KAKAO_SERVER_ERROR);
        }
    }



}
