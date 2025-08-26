package com.hanium.mom4u.global.redis.config;

import com.hanium.mom4u.global.redis.common.RedisStreamNames;
import com.hanium.mom4u.global.redis.consumer.RedisStreamConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisStreamConsumer redisStreamConsumer;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer() {
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(2))       // XREADGROUP block 시간
                .errorHandler(t -> log.error("Stream container error", t))
                .build();

        var container = StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // Consumer Name 설정 (UUID 이용)
        String consumerName = "spring-" + UUID.randomUUID();

        // 구독 시작
        var subscription = container.receive(
                Consumer.from(RedisStreamNames.SPRING_GROUP, consumerName),
                StreamOffset.create(RedisStreamNames.RESULT_STREAM, ReadOffset.lastConsumed()),
                redisStreamConsumer
        );

        container.start();
        return container;
    }
}
