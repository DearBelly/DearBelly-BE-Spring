package com.hanium.mom4u.external.redis.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.scan.dto.response.ModelResponseDto;
import com.hanium.mom4u.domain.scan.registry.PendingSinkRegistry;
import com.hanium.mom4u.external.redis.common.RedisStreamNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * "image.results" Stream을 구독
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class ImageResultStreamConsumer implements RedisStreamConsumer{

    private final PendingSinkRegistry registry;
    private final ObjectMapper objectMapper;

    private final String springConsumerName = "spring-consumer" + UUID.randomUUID();

    @Override
    public String streamKey() {
        return RedisStreamNames.RESULT_STREAM;
    }

    @Override
    public String groupName() {
        return RedisStreamNames.SPRING_GROUP;
    }

    @Override
    public String consumerName() {
        return springConsumerName;
    }

    /**
     * "image.results"에 대한 onMessage 처리
     * @param : Record (Redis Stream을 통해 받은 Message)
     * @return : Mono 로 0 혹은 1개의 데이터만 처리, 데이터는 ModelResponseDto
     */
    @Override
    public Mono<?> handleRecord(MapRecord<String, String, String> record) {
        Map<String, String> v = record.getValue();
        String type = v.get("type");
        String payload = v.get("payload");
        String correlationId = v.get("correlationId");

        // image_results가 아니면 처리하지 않음
        if (!"image_results".equals(type) || correlationId == null) {
            log.warn("drop invalid message id={} type={}", record.getId(), type);
            return Mono.empty(); // 0개의 데이터를 반환
        }

        return Mono.fromCallable(() -> objectMapper.readValue(payload, ModelResponseDto.class))
                .doOnSuccess(res -> {
                    registry.take(correlationId).ifPresent(sink -> {
                        sink.tryEmitValue(res);
                    });
                })
                .onErrorResume(ex -> {
                    log.info("image_results에서 일치하는 correlationId를 발견 못 함");
                    return Mono.empty();
                });
    }

}
