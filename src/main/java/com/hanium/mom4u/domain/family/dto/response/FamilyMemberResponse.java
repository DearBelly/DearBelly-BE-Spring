package com.hanium.mom4u.domain.family.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyMemberResponse {

    private String nickname;
    private String imgUrl;
    private boolean isPregnant;
}
