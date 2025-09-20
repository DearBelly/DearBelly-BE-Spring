package com.hanium.mom4u.domain.sse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
@Builder
public class MessageDto {
    @Schema(description = "알림을 받는 사람의 meberId")
    private Long receiverId;
    @Schema(description = "알림의 제목")
    private String title;
    @Schema(description = "알림의 내용", example = "새로운 편지가 도착했어요!")
    private String content;
}
