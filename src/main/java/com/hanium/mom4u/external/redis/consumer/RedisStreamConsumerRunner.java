package com.hanium.mom4u.external.redis.consumer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumerRunner {

    private final StreamReceiver<String, MapRecord<String, String, String>> streamReceiver;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final List<RedisStreamConsumer> consumers; // 모든 구현체의 Consumer 주입

    /**
     * 초기화 : 모든 주입 구독 시작
     */
    @PostConstruct
    public void init() {
        Flux.fromIterable(consumers)
                .flatMap(this::startOne)
                .subscribe();
    }

    private Mono<?> startOne(RedisStreamConsumer consumer) {
        // 1) 그룹 생성
        return reactiveStringRedisTemplate.opsForStream()
                .createGroup(consumer.streamKey(), ReadOffset.latest(), consumer.groupName())
                .onErrorResume(ex -> {
                    // 이미 그룹이 있으면 Skip
                    log.info("[Init Redis Stream Runner] group may already exist: stream={}, group={}, reason={}",
                            consumer.streamKey(), consumer.groupName(), ex.toString());
                    return Mono.empty();
                })
                // 2) Subscription
                .thenMany(
                        streamReceiver.receiveAutoAck(
                                        Consumer.from(consumer.groupName(), consumer.consumerName()),
                                        StreamOffset.create(consumer.streamKey(), ReadOffset.lastConsumed())
                                )
                                .flatMap(consumer::handleRecord)
                                .onErrorContinue((ex, obj) -> log.warn("stream receive error: {}", ex.toString(), ex))
                )
                .then(); // Mono 완료 신호
    }
}
