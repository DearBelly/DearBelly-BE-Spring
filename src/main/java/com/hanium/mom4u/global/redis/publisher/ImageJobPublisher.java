package com.hanium.mom4u.global.redis.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.redis.message.ImageJobMessage;
import com.hanium.mom4u.global.redis.common.RedisStreamNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageJobPublisher {

    private final RedisTemplate<String, String> redisStringTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 특정 메세지를 Redis Stream에 Publish 하기 -> FastAPI에서 Pop 필요
     * @param imageJobMessage
     */
    public RecordId publish(ImageJobMessage imageJobMessage) {
        try {
            String payload = objectMapper.writeValueAsString(imageJobMessage);
            Map<String, String> fields = Map.of(
                    "type", "image_job",
                    "payload", payload
            );
            return redisStringTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .in(RedisStreamNames.JOB_STREAM)
                            .ofMap(fields)
            );
        } catch (Exception e) {
            throw new IllegalStateException("XADD to " + RedisStreamNames.JOB_STREAM + " failed", e);
        }
    }
}
