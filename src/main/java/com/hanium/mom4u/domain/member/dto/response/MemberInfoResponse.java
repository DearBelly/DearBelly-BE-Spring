package com.hanium.mom4u.domain.member.dto.response;

import com.hanium.mom4u.domain.news.common.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public class MemberInfoResponse {
    private String nickname;
    private String email;
    private String imgUrl;
    private Boolean isPregnant;
    private LocalDate LmpDate;
    private Boolean prePregnant;
    private String gender;
    private LocalDate birthDate;
    private Set<Category> interests;
    private String socialType;
}

