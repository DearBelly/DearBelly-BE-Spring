package com.hanium.mom4u.external.redis.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.external.redis.message.ImageJobMessage;
import com.hanium.mom4u.external.redis.common.RedisStreamNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageJobPublisher {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Mono 객체를 통한 비동기 발행
     * ImageJobMessage Redis Stream에 Publish 하기 -> FastAPI에서 구독 후 처리
     * @param imageJobMessage
     */
    public Mono<RecordId> publish(ImageJobMessage imageJobMessage) {

        // 검증
        Objects.requireNonNull(imageJobMessage, "ImageJobMessage must not be null");
        Objects.requireNonNull(imageJobMessage.getCorrelationId(), "correlationId must not be null");

        // 직렬화 후 XADD로 메시지 발행
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(imageJobMessage))
                .map(payload -> {
                    Map<String, String> fields = new HashMap<>();
                    fields.put("type", "image_job");
                    fields.put("correlationId", imageJobMessage.getCorrelationId());
                    fields.put("payload", payload);
                    return fields;
                })
                .flatMap(fields -> {
                    var record = StreamRecords.newRecord()
                            .in(RedisStreamNames.JOB_STREAM)
                            .ofMap(fields);
                    return reactiveStringRedisTemplate.opsForStream().add(record);
                })
                .onErrorMap(e -> new IllegalStateException("XADD to " + RedisStreamNames.JOB_STREAM + " failed", e));
    }
}
