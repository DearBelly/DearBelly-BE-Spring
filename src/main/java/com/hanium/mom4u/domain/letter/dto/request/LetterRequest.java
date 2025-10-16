package com.hanium.mom4u.domain.letter.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LetterRequest {

    @Size(max = 300, message = "내용은 300자 이하여야 합니다.")
    private String content;
}
