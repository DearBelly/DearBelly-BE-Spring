package com.hanium.mom4u.domain.scan.dto.response;

import lombok.*;

@Getter
@RequiredArgsConstructor
@Builder
public class UploadResponseDto {

    private final String correlationId;
    private final String objectKey;
}
