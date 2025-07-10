package com.hanium.mom4u.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MemberInfoResponse {
    private String nickname;
    private String email;
    private String imgUrl;
    private Boolean isPregnant;
    private LocalDate dueDate;
    private Boolean prePregnant;
    private String gender;
    private LocalDate birthDate;
    private String socialType;
}

