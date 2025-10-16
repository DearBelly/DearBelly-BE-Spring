package com.hanium.mom4u.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "프로필 이미지 커밋 요청 바디")
public record ImageCommitRequest(
        @Schema(description = "업로드 완료된 S3 objectKey", example = "images/14/qq")
        String objectKey
) {}
