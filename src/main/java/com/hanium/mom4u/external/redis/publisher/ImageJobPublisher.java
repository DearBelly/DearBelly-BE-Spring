package com.hanium.mom4u.external.redis.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.external.redis.message.ImageJobMessage;
import com.hanium.mom4u.external.redis.common.RedisStreamNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageJobPublisher {

    private final RedisTemplate<String, String> redisStringTemplate;
    private final ObjectMapper objectMapper;


    /**
     * 동기식으로 Redis Stream(image.jobs)에 작업 발행하기
     * @param imageJobMessage
     * @return
     */
    public RecordId publish(ImageJobMessage imageJobMessage) {

        // 검증
        Objects.requireNonNull(imageJobMessage, "ImageJobMessage must not be null");
        Objects.requireNonNull(imageJobMessage.getCorrelationId(), "correlationId must not be null");

        // XADD로 발행하기
        try {
            // payload 직렬화
            String payload = objectMapper.writeValueAsString(imageJobMessage);

            Map<String, String> fields = new HashMap<>();
            fields.put("type", "image_jobs");
            fields.put("correlationId", imageJobMessage.getCorrelationId());
            fields.put("payload", payload);

            // MapRecord 생성
            MapRecord<String, String, String> record = StreamRecords
                    .mapBacked(fields)
                    .withStreamKey(RedisStreamNames.JOB_STREAM);

            // stream 길이 제한 옵션: MAXLEN ~ 10000
            RedisStreamCommands.XAddOptions options = RedisStreamCommands.XAddOptions.maxlen(10_000).approximateTrimming(true);

            // XADD
            RecordId id = redisStringTemplate.opsForStream().add(record, options);
            if (id == null) {
                throw new IllegalStateException("XADD returned null RecordId");
            }

            // XADD 성공
            log.info("XADD OK stream={} id={}", RedisStreamNames.JOB_STREAM, id.getValue());
            return id;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize ImageJobMessage", e);
        } catch (Exception e) {
            throw new IllegalStateException("XADD to " + RedisStreamNames.JOB_STREAM + " failed", e);
        }
    }
}
