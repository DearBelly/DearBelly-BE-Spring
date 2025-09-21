package com.hanium.mom4u.external.redis.publisher;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Redis Pub/Sub 기반 Publisher
 * Message에 대하여 Publish -> Subscriber가 수신
 */
@Component
@Slf4j
@AllArgsConstructor
public class MessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String channel, Object message) {
        log.info("Publishing message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        redisTemplate.convertAndSend(channel, message);
        log.info("Published message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
    }
}
