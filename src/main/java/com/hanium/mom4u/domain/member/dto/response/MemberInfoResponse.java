package com.hanium.mom4u.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate lmpDate;
    private Boolean prePregnant;
    private String gender;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate birthDate;
    private Set<Category> interests;
    private String socialType;
}

