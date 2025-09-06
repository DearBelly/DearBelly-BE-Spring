package com.hanium.mom4u.external.redis.common;

import org.springframework.stereotype.Component;

/**
 * Consumer, Publisher 그룹 이름 참조
 */
@Component
public class RedisStreamNames {
    public static final String JOB_STREAM = "image.jobs";         // SpringBoot → FastAPI 작업 요청
    public static final String RESULT_STREAM = "image.results";   // FastAPI → SpringBoot 결과

    public static final String FASTAPI_GROUP = "fastapi-workers";     // FastAPI 그룹
    public static final String SPRING_GROUP  = "spring-consumers";    // Spring Consumer 그룹

    public static final String DLQ_RESULTS = "image.results.dlq";     // 실패시 DLQ
}
