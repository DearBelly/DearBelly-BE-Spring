package com.hanium.mom4u.domain.question.controller;

import com.hanium.mom4u.domain.question.dto.response.HomeResponse;
import com.hanium.mom4u.domain.question.service.HomeService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "홈 페이지")
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "홈 상단 배너 조회",
            description = "아기 이름, 오늘 기준 0주차부터 계산된 주차, 그리고 '남이 쓴 미열람 편지' 여부를 반환합니다."
    )
    @GetMapping("/top")
    public ResponseEntity<CommonResponse<HomeResponse>> top() {
        return ResponseEntity.ok(CommonResponse.onSuccess(homeService.getTopBanner()));
    }



}
