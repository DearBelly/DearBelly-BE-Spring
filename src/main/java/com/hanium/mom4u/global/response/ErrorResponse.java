package com.hanium.mom4u.global.response;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private HttpStatus httpStatus;
    private String code;
    private String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(StatusCode statusCode) {
        return ResponseEntity
                .status(statusCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .httpStatus(statusCode.getHttpStatus())
                        .code(statusCode.getCode())
                        .message(statusCode.getDescription())
                        .build());
    }
}
