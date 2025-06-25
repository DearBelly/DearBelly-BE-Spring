package com.hanium.mom4u.global.security.controller;

import com.hanium.mom4u.global.response.CommonResponse;
import com.hanium.mom4u.global.security.service.GoogleLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "구글 로그인 API Controller")
public class GoogleLoginController {

    private final GoogleLoginService googleLoginService;

    @GetMapping("/url/google")
    @Operation(summary = "구글 로그인 URL 조회 API", description = """
            해당 URL로 이동 시 구글 서버로부터 인증 코드를 얻을 수 있습니다.
            """)
    public ResponseEntity<CommonResponse<String>> getUrl() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(googleLoginService.getCode()));
    }

    @PostMapping("/google")
    @Operation(summary = "구글 로그인 API", description = """
            구글 로그인 API 입니다.
            Parameter로는 code 파싱한 값을 입력해주세요.
            """)
    public ResponseEntity<CommonResponse<?>> googleLogin(
            HttpServletResponse response,
            @RequestParam("code") String code
    ) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(googleLoginService.loginWithGoogle(response, code))
        );
    }
}
