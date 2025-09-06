package com.hanium.mom4u.external.redis.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;

import java.time.Duration;

/**
 * Reactive Redis Stream Consumer listener(receiver)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConfig {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Bean
    public StreamReceiver<String, MapRecord<String, String, String>> streamReceiver() {

        StreamReceiver.StreamReceiverOptions<String, MapRecord<String, String, String>> options =
                StreamReceiver.StreamReceiverOptions.builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        return StreamReceiver.create(reactiveStringRedisTemplate.getConnectionFactory(), options);
    }
}
