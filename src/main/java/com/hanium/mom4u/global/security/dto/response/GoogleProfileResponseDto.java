package com.hanium.mom4u.global.security.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleProfileResponseDto {

    @JsonProperty("id")
    public String id;
    @JsonProperty("email")
    public String email;
    @JsonProperty("verified_email")
    public boolean verifiedEmail;
    @JsonProperty("name")
    public String name;
    @JsonProperty("given_name")
    public String givenName;
    @JsonProperty("family_name")
    public String familyName;
    @JsonProperty("picture")
    public String picture;
}
