package com.hanium.mom4u.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Builder
@Getter
@AllArgsConstructor
@Schema(description = "편지 조회 관련 응답 DTO")
public class LetterResponse {

    @Schema(name = "편지 고유 ID")
    private Long id;
    @Schema(name = "편지 내용")
    private String content;
    @Schema(name = "작성자 닉네임")
    private String nickname;
    @Schema(name = "작성자 프로필 사진")
    private String imgUrl;

    @Schema(name = "작성 날짜")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDateTime createdAt;
    @Schema(name = "내가 쓴 글인지의 여부(본인 작성자인지)")
    private boolean editable; // 내가 쓴 글인지
    @Schema(name = "")
    private String question;


}
