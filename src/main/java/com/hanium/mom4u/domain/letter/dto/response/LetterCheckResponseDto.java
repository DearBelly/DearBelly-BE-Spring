package com.hanium.mom4u.domain.letter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "하루에 읽은 편지 확인 응답 DTO")
public class LetterCheckResponseDto {

    @Schema(name = "회원의 ID")
    private Long memberId;
    @Schema(name = "읽지 않은 것이 존재하는지에 대한 여부")
    private boolean isUnreadExist;
}
