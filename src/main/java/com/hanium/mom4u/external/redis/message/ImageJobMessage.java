package com.hanium.mom4u.external.redis.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageJobMessage {

    private String correlationId;
    private String presignedUrl;
    private String replyQueue;
    private String callbackUrl;
    private String contentType;
    private String createdAt;
    private long ttlSec;
}
