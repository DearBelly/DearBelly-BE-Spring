package com.hanium.mom4u.external.redis.subscribe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.sse.dto.MessageDto;
import com.hanium.mom4u.domain.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub에 대한 구독 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            MessageDto messageDto = objectMapper.readValue(body, MessageDto.class);
            log.info("Received alarm via Redis: {}", messageDto);

            sseService.send(messageDto);
        } catch (Exception e) {
            log.error("Failed to handle Redis message", e, e.getMessage());
        }
    }
}
