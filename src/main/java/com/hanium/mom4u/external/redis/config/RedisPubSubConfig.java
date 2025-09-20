package com.hanium.mom4u.external.redis.config;

import com.hanium.mom4u.external.redis.subscribe.MessageSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Pub/Sub 기반 Channel 설정
 */
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final MessageSubscriber messageSubscriber;
    private final LettuceConnectionFactory redisConnectionFactory;

    private static final String CHANNEL_NAME = "Alarm";

    // Listener 설정
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(messageSubscriber, new ChannelTopic(CHANNEL_NAME));

        return container;
    }
}
