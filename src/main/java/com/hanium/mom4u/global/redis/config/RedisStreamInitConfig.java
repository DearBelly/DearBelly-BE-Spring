package com.hanium.mom4u.global.redis.config;

import com.hanium.mom4u.global.redis.common.RedisStreamNames;
import io.lettuce.core.RedisBusyException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.util.Collections;

/**
 * 초기에 실행 시 그룹이 없을 때의 예외 처리
 */
@Configuration
@RequiredArgsConstructor
public class RedisStreamInitConfig {

    private final RedisTemplate<String, String> redisStringTemplate;

    /**
     * 시작 시에 확실한 Stream Group 보장을 위한 로직
     * @return
     */
    @Bean
    ApplicationRunner initStreamAndGroup() {
        return args -> {
            final String stream = RedisStreamNames.RESULT_STREAM; // stream key
            final String group  = RedisStreamNames.SPRING_GROUP; // consumer group name

            StreamOperations<String, String, String> ops = redisStringTemplate.opsForStream();

            // dummy XADD
            if (Boolean.FALSE.equals(redisStringTemplate.hasKey(stream))) {
                ops.add(StreamRecords.mapBacked(Collections.emptyMap()).withStreamKey(stream));
            }

            // 그룹이 이미 있으면 Skip (XINFO GROUPS)
            boolean exists = false;
            try {
                for (StreamInfo.XInfoGroup g : ops.groups(stream)) {
                    if (group.equals(g.groupName())) {
                        exists = true;
                        break;
                    }
                }
            } catch (DataAccessException ignore) {} // skip

            if (!exists) {
                try {
                    ops.createGroup(stream, ReadOffset.latest(), group);
                } catch (RedisSystemException e) { // Busygroup Error
                    Throwable root = getRootCause(e);
                    if (!(root instanceof RedisBusyException)
                            || !String.valueOf(root.getMessage()).contains("BUSYGROUP")) {
                        throw e;
                    }
                }
            }
        };
    }

    /**
     * 원인 분석을 위한 메서드
     * @param t
     * @return
     */
    private static Throwable getRootCause(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null && c.getCause() != c) c = c.getCause();
        return c;
    }
}
