package com.hanium.mom4u.domain.member.controller;

import com.hanium.mom4u.domain.member.dto.request.ThemeRequest;
import com.hanium.mom4u.domain.member.dto.response.ThemeResponse;
import com.hanium.mom4u.domain.member.service.MemberService;
import com.hanium.mom4u.domain.member.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @Operation(summary = "테마 조회", description = "현재 웹의 테마를 조회합니다.")
    @GetMapping("/theme")
    public ResponseEntity<ThemeResponse> getTheme() {
        return ResponseEntity.ok(new ThemeResponse(settingsService.getLightMode()));
    }

    @Operation(summary = "회원 테마 변경", description = "라이트 모드로 설정하려면 true, 다크 모드로 설정하려면 false를 입력하세요.")
    @PostMapping("/theme")
    public ResponseEntity<Void> updateTheme(@RequestBody ThemeRequest request) {
        settingsService.updateLightMode(request.getTheme());
        return ResponseEntity.ok().build();
    }

}

