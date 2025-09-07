package com.hanium.mom4u.external.redis.consumer;

import org.springframework.data.redis.connection.stream.MapRecord;
import reactor.core.publisher.Mono;

public interface RedisStreamConsumer {

        /** 구독할 스트림 키 */
        String streamKey();

        /** 사용할 컨슈머 그룹 */
        String groupName();

        /** 컨슈머 인스턴스 이름 (같은 그룹 내에서 유니크 권장) */
        String consumerName();

        /** 레코드 처리 로직 */
        Mono<?> handleRecord(MapRecord<String, String, String> record);

}
