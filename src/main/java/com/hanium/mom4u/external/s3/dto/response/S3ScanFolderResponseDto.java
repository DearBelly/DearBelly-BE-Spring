package com.hanium.mom4u.external.s3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class S3ScanFolderResponseDto {

    private String presignedUrl;
    private String objectKey;
}
