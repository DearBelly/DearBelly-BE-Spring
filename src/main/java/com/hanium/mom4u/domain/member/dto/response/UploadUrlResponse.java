package com.hanium.mom4u.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "S3 업로드용 Presigned URL 응답")
public record UploadUrlResponse(
        @Schema(description = "S3 PUT Presigned URL")
        String putUrl,
        @Schema(description = "업로드될 S3 objectKey", example = "images/14/qq")
        String objectKey
) {}

