package com.hanium.mom4u.domain.question.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeResponse {
    private String babyName;          // 아기 이름
    private int week;                 // 오늘 기준 0부터
    private boolean hasUnreadLetters; // true면 편지 아이콘 표시

}
