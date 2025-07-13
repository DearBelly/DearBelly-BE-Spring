package com.hanium.mom4u.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.dto.response.NaverProfileResponseDto;
import com.hanium.mom4u.global.security.dto.response.NaverTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
@Slf4j
public class NaverUtil {

    @Value("${spring.security.naver.client_id}")
    private String clientId;

    @Value("${spring.security.naver.redirect_uri}")
    private String redirectUri;

    @Value("${spring.security.naver.secret_key}")
    private String secretKey;

    private static final String NAVER_AUTH_URI = "https://nid.naver.com/oauth2.0/authorize";
    private static final String NAVER_TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URI = "https://openapi.naver.com/v1/nid/me";

    private String generateState()
    {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public String buildLoginUrl() {

        String state = generateState();
        return String.format(
                "%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s",
                NAVER_AUTH_URI, clientId, redirectUri, state
        );
    }

    public NaverTokenDto getToken(String code, String state) {

        // RequestBody (param)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("client_secret", secretKey);
        params.add("code", code);
        params.add("state", state);

        // Request
        WebClient webClient = WebClient.create();

        String response;
        try {
            response = webClient.post()
                    .uri(NAVER_TOKEN_URI)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to retrieve token from Naver", e);
            throw GeneralException.of(StatusCode.NAVER_TOKEN_ERROR);
        }

        try {
            NaverTokenDto token = new ObjectMapper().readValue(response, NaverTokenDto.class);
            log.info("Naver token response: {}", token);
            return token;
        } catch (JsonProcessingException e) {
            log.error("Naver token parse error. Raw response: {}", response, e);
            throw GeneralException.of(StatusCode.NAVER_PARSE_ERROR);
        }
    }

    // UserInfo 가져오기
    public NaverProfileResponseDto findProfile(String token) {

        WebClient webClient = WebClient.create();
        String response;
        try {
            response = webClient.get()
                    .uri(NAVER_PROFILE_URI)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Naver profile raw response: {}", response);
        } catch (Exception e) {
            log.error("네이버 프로필 API 호출 실패", e);
            throw GeneralException.of(StatusCode.NAVER_PROFILE_REQUEST_FAILED);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, NaverProfileResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("Naver profile 파싱 실패. 원본 응답: {}", response, e);
            throw GeneralException.of(StatusCode.NAVER_PROFILE_FAILED);
        }
    }
}
