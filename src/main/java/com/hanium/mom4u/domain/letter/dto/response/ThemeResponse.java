package com.hanium.mom4u.domain.letter.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThemeResponse {
    @Schema(description="현재 테마", example="MINT")
    private String theme;
}

