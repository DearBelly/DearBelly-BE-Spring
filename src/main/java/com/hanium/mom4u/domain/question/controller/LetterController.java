package com.hanium.mom4u.domain.question.controller;

import com.hanium.mom4u.domain.question.dto.request.LetterRequest;
import com.hanium.mom4u.domain.question.dto.response.HomeResponse;
import com.hanium.mom4u.domain.question.dto.response.LetterResponse;
import com.hanium.mom4u.domain.question.service.LetterService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/letters")
@RequiredArgsConstructor
@Tag(name = "홈 페이지")
public class LetterController {

    private final LetterService letterService;

    @Operation(
            summary = "홈 상단 배너 조회",
            description = "아기 이름, 오늘 기준 0주차부터 계산된 주차, 그리고 '남이 쓴 미열람 편지' 여부를 반환합니다."
    )
    @GetMapping("/top")
    public ResponseEntity<CommonResponse<HomeResponse>> top() {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.getTopBanner()));
    }

    @Operation(summary = "편지 쓰기 API", description = "편지 작성 API입니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<Long>> create(@RequestBody @Valid LetterRequest request) {
        Long letterId = letterService.create(request);
        return ResponseEntity.ok(CommonResponse.onSuccess(letterId));
    }

    @Operation(
            summary = "편지 월별 조회 API",
            description = "같은 가족의 편지를 월 단위로 조회합니다. year/month 미지정 시 이번 달을 기준으로 합니다."
    )
    @GetMapping
    public ResponseEntity<CommonResponse<List<LetterResponse>>> getByMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<LetterResponse> responses = letterService.getByMonth(year, month);
        return ResponseEntity.ok(CommonResponse.onSuccess(responses));
    }

    @Operation(
            summary = "편지 상세 조회 API",
            description = "편지 1건을 조회합니다. 조회 시 현재 사용자 기준으로 '남이 쓴 글'만 읽음 처리됩니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<LetterResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.getDetail(id)));
    }


    @Operation(
            summary = "편지 수정 API", description = "작성자 본인만 자신의 편지를 수정할 수 있습니다.")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable Long id, @RequestBody @Valid LetterRequest request) {
        letterService.update(id, request);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "편지 삭제 API", description = "작성자 본인만 삭제할 수 있습니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        letterService.delete(id);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }
}


