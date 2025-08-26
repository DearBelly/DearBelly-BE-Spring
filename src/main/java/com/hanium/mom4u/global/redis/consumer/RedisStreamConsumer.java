package com.hanium.mom4u.global.redis.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.scan.dto.response.ModelResponseDto;
import com.hanium.mom4u.domain.scan.service.ScanService;
import com.hanium.mom4u.global.redis.common.RedisStreamNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;

@RequiredArgsConstructor
@Slf4j
@Component
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final RedisTemplate<String, String> redisStringTemplate;
    private final ObjectMapper objectMapper;

    private final ScanService scanService;

    // TODO: DisposableBean 으로 예외처리 추가

    /**
     * Redis Stream 메세지를 처리하는 메소드
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        String stream = message.getStream();
        RecordId id = message.getId();

        try {
            String type = message.getValue().get("type");
            String payload = message.getValue().get("payload");

            if (!"image_result".equals(type)) {
                // 타입 불명 → DLQ로 복제 후 ACK
                toDlq(message, "UNKNOWN_TYPE");
                ack(stream, id);
                return;
            }

            String body = new String(payload.getBytes(), StandardCharsets.UTF_8);
            log.info("📥 분석 결과 수신: {}", body);

            // JSON → DTO 변환
            ModelResponseDto result = objectMapper.readValue(body, ModelResponseDto.class);

            // 중복을 방지
            boolean first = tryMarkProcessedOnce(result.getCorrelationId());
            if (!first) {
                log.warn("Duplicate result skipped: {}", result.getCorrelationId());
                ack(stream, id);
                return;
            }
            scanService.processResult(result);

            // 정상 처리 후 ACK
            ack(stream, id);

        } catch (Exception e) {
            log.error("Redis 구독 처리 중 오류", e);
        }
    }

    private void ack(String stream, RecordId id) {
        redisStringTemplate.opsForStream().acknowledge(stream, RedisStreamNames.SPRING_GROUP, id);
    }

    private void toDlq(MapRecord<String, String, String> msg, String reason) {
        var map = new HashMap<>(msg.getValue());
        map.put("reason", reason);
        map.put("originalStream", msg.getStream());
        map.put("originalId", msg.getId().getValue());
        redisStringTemplate.opsForStream().add(StreamRecords.newRecord()
                .in(RedisStreamNames.DLQ_RESULTS)
                .ofMap(map));
    }

    private boolean tryMarkProcessedOnce(String correlationId) {
        String key = "processed:" + correlationId;
        Boolean set = redisStringTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofHours(6));
        return Boolean.TRUE.equals(set);
    }
}
