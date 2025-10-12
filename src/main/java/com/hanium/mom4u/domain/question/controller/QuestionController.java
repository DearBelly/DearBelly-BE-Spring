package com.hanium.mom4u.domain.question.controller;

import com.hanium.mom4u.domain.letter.dto.response.TodayWriteResponse;
import com.hanium.mom4u.domain.letter.service.LetterService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name="질문 API Controller")
public class QuestionController {

    private final LetterService letterService;

    @Operation(summary = "오늘의 질문 + 내 오늘 편지 상태")
    @GetMapping("/today")
    public ResponseEntity<CommonResponse<TodayWriteResponse>> today() {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.getTodayForWrite()));
    }
}
