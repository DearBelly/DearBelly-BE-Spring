package com.hanium.mom4u.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.dto.response.GoogleProfileResponseDto;
import com.hanium.mom4u.global.security.dto.response.GoogleTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class GoogleUtil {

    @Value("${spring.security.google.client_id}")
    private String clientId;

    @Value("${spring.security.google.redirect_uri}")
    private String redirectUri;

    @Value("${spring.security.google.secret_key}")
    private String secretKey;

    private static final String GOOGLE_AUTH_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_PROFILE_URI = "https://www.googleapis.com/oauth2/v2/userinfo";

    public String buildLoginUrl() {
        return String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                GOOGLE_AUTH_URI, clientId, redirectUri, "email profile");
    }

    public GoogleTokenDto getToken(String code) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", secretKey);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        WebClient webClient = WebClient.create();

        String response;
        try {
            response = webClient.post()
                    .uri(GOOGLE_TOKEN_URI)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to retrieve token from Google", e);
            throw GeneralException.of(StatusCode.GOOGLE_TOKEN_ERROR);
        }

        try {
            GoogleTokenDto token = new ObjectMapper().readValue(response, GoogleTokenDto.class);
            log.info("Google token response: {}", token);
            return token;
        } catch (JsonProcessingException e) {
            log.error("Google token parse error. Raw response: {}", response, e);
            throw GeneralException.of(StatusCode.GOOGLE_PARSE_ERROR);
        }
    }

    // 사용자 데이터 가져오기
    public GoogleProfileResponseDto findProfile(String token) {

        WebClient webClient = WebClient.create();
        String response;
        try {
            response = webClient.get()
                    .uri(GOOGLE_PROFILE_URI)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Google profile raw response: {}", response);
        } catch (Exception e) {
            log.error("Google 사용자 정보 호출 실패", e);
            throw GeneralException.of(StatusCode.GOOGLE_PROFILE_REQUEST_FAILED);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, GoogleProfileResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("Google 사용자 정보 파싱 실패. 원본 응답: {}", response, e);
            throw GeneralException.of(StatusCode.GOOGLE_PROFILE_FAILED);
        }
    }
}
