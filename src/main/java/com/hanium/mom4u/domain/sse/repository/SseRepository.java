package com.hanium.mom4u.domain.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseRepository {

    // key: memberId(String) -> emitters
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> store = new ConcurrentHashMap<>();

    // 키를 통하여 원하는 SSE 삭제하기
    public void remove(String memberKey, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = store.get(memberKey);
        if (list == null) return;
        list.remove(emitter);
        if (list.isEmpty()) store.remove(memberKey, list);
    }

    // key에 새로운 SSE를 등록
    public void add(String memberKey, SseEmitter emitter) {
        store.computeIfAbsent(memberKey, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    // key를 기반으로 조회하기
    public Collection<SseEmitter> get(String memberKey) {
        return store.getOrDefault(memberKey, new CopyOnWriteArrayList<>());
    }

    // key에 해당하는 모든 SSE 찾기
    public void removeByEmitter(SseEmitter emitter) {
        // 전체 키를 스캔해 제거(수는 적으므로 괜찮음. 대규모면 역인덱스 추가)
        store.forEach((k, v) -> {
            if (v.remove(emitter) && v.isEmpty()) {
                store.remove(k, v);
            }
        });
    }
}
