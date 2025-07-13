package com.hanium.mom4u.global.security.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverProfileResponseDto {

    @JsonProperty("resultcode")
    public String resultCode;
    @JsonProperty("message")
    public String message;
    @JsonProperty("response")
    public Response response;

    @Getter
    public static class Response {
        @JsonProperty("id")
        public String id;
        @JsonProperty("nickname")
        public String nickname;
        @JsonProperty("age")
        public String age;
        @JsonProperty("gender")
        public String gender;
        @JsonProperty("email")
        public String email;
        @JsonProperty("name")
        public String name;
        @JsonProperty("mobile")
        public String mobile;
        @JsonProperty("mobile_e164")
        public String globalMobile;
        @JsonProperty("birthday")
        public String birthday;
        @JsonProperty("birthyear")
        public String birthyear;
    }
}