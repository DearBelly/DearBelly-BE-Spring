package com.hanium.mom4u.global.security.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponseDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        @JsonProperty("email")
        private String email;

        @JsonProperty("profile")
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            @JsonProperty("nickname")
            private String nickname;
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }
}
