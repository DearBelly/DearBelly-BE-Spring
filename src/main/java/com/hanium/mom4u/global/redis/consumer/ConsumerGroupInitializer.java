package com.hanium.mom4u.global.redis.consumer;

import com.hanium.mom4u.global.redis.common.RedisStreamNames;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class ConsumerGroupInitializer implements InitializingBean {

    private final RedisTemplate<String, String> redisStringTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        createGroupIfNotExists(RedisStreamNames.RESULT_STREAM, RedisStreamNames.SPRING_GROUP);
    }

    private void createGroupIfNotExists(String stream, String group) {
        try {
            // XGROUP CREATE stream group $ MKSTREAM
            redisStringTemplate.opsForStream().createGroup(stream, ReadOffset.from("0-0"), group);
        } catch (Exception e) {
            // 이미 있으면 에러남 → Skip 하기
        }
    }
}
