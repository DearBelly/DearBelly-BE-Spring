package com.hanium.mom4u.domain.question.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Builder
@Getter
@AllArgsConstructor
public class LetterResponse {

    private Long id;
    private String content;
    private String nickname;
    private String imgUrl;
    private LocalDateTime createdAt;
    private boolean editable; // 내가 쓴 글인지

}
