package com.hanium.mom4u.domain.sse.service;

import com.hanium.mom4u.domain.sse.dto.MessageDto;
import com.hanium.mom4u.domain.sse.repository.SseRepository;
import com.hanium.mom4u.external.redis.publisher.MessagePublisher;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final SseRepository sseRepository;

    private final AuthenticatedProvider authenticatedProvider;
    private final MessagePublisher messagePublisher;

    private static final String EVENT_NAME_ALARM = "ALARM";

    /*
    기본 SSE 구독 (SSE 없으면 새로 생성, 있으면 계속 연결)
     */
    public SseEmitter subscribe(String lastEventId) {

        LocalDateTime now = LocalDateTime.now();
        Long memberId = authenticatedProvider.getCurrentMemberId();

        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // emitter 등록
        sseRepository.add(String.valueOf(memberId), emitter);

        // callback
        emitter.onCompletion(() -> {
            log.info("[SSE] onCompletion: memberId={}", memberId);
            sseRepository.remove(String.valueOf(memberId), emitter);
        });
        // timeOut 처리
        emitter.onTimeout(() -> {
            log.info("[SSE] onTimeout: memberId={}", memberId);
            sseRepository.remove(String.valueOf(memberId), emitter);
            emitter.complete();
        });
        // Error 처리
        emitter.onError((Throwable t) -> {
            log.warn("[SSE] onError: memberId={}, err={}", memberId, t.toString());
            sseRepository.remove(String.valueOf(memberId), emitter);
        });

        // 최초 연결
        try {
            emitter.send(SseEmitter.event()
                    .name("SUBSCRIBE")
                    .id(getEventId(memberId, now))
                    .data("subscribed:" + memberId));
        } catch (Exception e) {
            sseRepository.remove(String.valueOf(memberId), emitter);
            log.error("[SSE] subscribe send fail: {}", e.getMessage(), e);
            throw new GeneralException(StatusCode.SSE_CONNECTION_ERROR);
        }
        return emitter;
    }

    /*
    SSE 별로 생성할 고유한 ID(Event 필터링에 사용)
     */
    private String getEventId(Long memberId, LocalDateTime now) {
        return memberId + "_" + now;
    }

    /*
    기존의 SSE에 이어서 보내기
     */
    public void send(MessageDto messageDto) {

        Long receiverId = (messageDto.getReceiverId() != null) ? messageDto.getReceiverId() : null;

        // receiverId가 비어있을 때
        if (receiverId == null) {
            // skip
            log.info("[SSE] skip: receiverId is null. dto={}", messageDto);
            return;
        } else  {

            // memberId에 등록되어있는 모든 SSE(in local Instance)
            Collection<SseEmitter> targets = sseRepository.get(String.valueOf(receiverId));

            if (targets == null || targets.isEmpty()) {
                log.warn("[SSE] no active emitters for receiverId={}, dto={}", receiverId, messageDto);

                messagePublisher.publish("Alarm", messageDto);
                return;
            }
            log.info("[SSE] send to {} emitters for receiverId={}", targets.size(), receiverId);
            broadcast(targets, EVENT_NAME_ALARM, messageDto, receiverId);
        }
    }

    /*
    기존의 SSE List에 보낼 대상을 추가
     */
    private void broadcast(Collection<SseEmitter> targets, String eventName, Object payload, Long memberId) {
        List<SseEmitter> toRemove = new ArrayList<>();
        for (SseEmitter emitter : targets) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .id(getEventId(memberId, LocalDateTime.now()))
                        .data(payload));
            } catch (Exception e) {
                log.debug("[SSE] emitter send fail. mark for removal. err={}", e.toString());
                toRemove.add(emitter);
            }
        }
        // 실패 emitter 제거
        toRemove.forEach(em -> sseRepository.removeByEmitter(em));
    }
}
