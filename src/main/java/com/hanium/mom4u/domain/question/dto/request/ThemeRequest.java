package com.hanium.mom4u.domain.question.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeRequest {
    @Schema(description="변경할 테마", example="SUNSET")
    @NotBlank
    private String theme;
}

