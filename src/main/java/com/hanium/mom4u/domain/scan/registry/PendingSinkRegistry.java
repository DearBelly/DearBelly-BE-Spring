package com.hanium.mom4u.domain.scan.registry;

import com.hanium.mom4u.domain.scan.dto.response.ModelResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mono 응답에 대한 대기열 작업 Registry
 * 대기열에 String CorrelationId도 추가하여 고유한 작업에 대하여 처리
 */
@Component
@Slf4j
public class PendingSinkRegistry {

    private final ConcurrentMap<String, Sinks.One<ModelResponseDto>> waits = new ConcurrentHashMap<>();

    /**
     * 새로운 작업 Sink 대상 추가
     * @param correlationId : 대기열에 있는 Sink 구분을 위한 것
     * @return
     */
    public Sinks.One<ModelResponseDto> create(String correlationId) {
        Sinks.One<ModelResponseDto> sink = Sinks.one();
        waits.put(correlationId, sink);
        return sink;
    }

    /**
     * 대기열에 있는 것을 꺼내는 메서드
     * @param correlationId
     * @return
     */
    public Optional<Sinks.One<ModelResponseDto>> take(String correlationId) {

        // 대기열에서 삭제 처리
        Sinks.One<ModelResponseDto> sink = waits.remove(correlationId);
        return Optional.ofNullable(sink);
    }

    /**
     * 타임아웃일 경우의 처리 -> take를 통해 Sink를 대기열에서 삭제한 후에 타임아웃으로 예외 던지기
     * @param correlationId
     */
    public void completeTimeout(String correlationId) {
        take(correlationId).ifPresent(sink -> {
            log.warn("제한 시간에 초과되어 저장소에서 삭제: {}", correlationId);
        });
    }
}