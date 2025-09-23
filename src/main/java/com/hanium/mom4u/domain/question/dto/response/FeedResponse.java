package com.hanium.mom4u.domain.question.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedResponse {
    private List<LetterResponse> items; // 각 항목에 question 포함
    private String nextCursor;
}
