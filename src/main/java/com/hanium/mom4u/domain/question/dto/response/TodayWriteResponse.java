package com.hanium.mom4u.domain.question.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayWriteResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate date;           // 오늘 날짜

    private Long questionId;          // DailyQuestion.id
    private String questionContent;   // DailyQuestion.question

    private Long myLetterId;
    private String myLetterContent;
    private boolean canWrite;         // 오늘 작성 가능 여부 (1일 1개)
    private boolean editable;         // 내 글이면 수정 가능
}
