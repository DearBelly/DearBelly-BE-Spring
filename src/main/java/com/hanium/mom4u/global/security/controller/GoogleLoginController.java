package com.hanium.mom4u.global.security.controller;

import com.hanium.mom4u.global.response.CommonResponse;
import com.hanium.mom4u.global.security.service.GoogleLoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GoogleLoginController {

    private final GoogleLoginService googleLoginService;

    @GetMapping("/url/google")
    public ResponseEntity<CommonResponse<String>> getUrl() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(googleLoginService.getCode()));
    }

    @PostMapping("/google")
    public ResponseEntity<CommonResponse<?>> googleLogin(
            HttpServletResponse response,
            @RequestParam("code") String code
    ) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(googleLoginService.loginWithGoogle(response, code))
        );
    }
}
