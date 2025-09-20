package com.hanium.mom4u.domain.sse.controller;

import com.hanium.mom4u.domain.sse.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@Tag(name = "SSE 연결 API Controller",
description = "SSE Connect를 위한 API Controller입니다.")
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE 연결 API",
            description = """
           SSE 연결을 위한 최초의 url입니다.<br>
           Header에는 Authorization으로 토큰만 넣어주시면 됩니다.<br>
           수신할 Event의 주 타입은 SUBSCRIBE, ALARM입니다. <br>
           서버에서 설정된 시간이 만료되지 않는 이상 생성된 event는 만료되지 않습니다.
           """)
    public SseEmitter subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId,
            HttpServletResponse response
    ) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        return sseService.subscribe(lastEventId);
    }
}
