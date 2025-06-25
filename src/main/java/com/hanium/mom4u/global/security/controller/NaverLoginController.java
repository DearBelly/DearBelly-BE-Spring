package com.hanium.mom4u.global.security.controller;

import com.hanium.mom4u.global.response.CommonResponse;
import com.hanium.mom4u.global.security.dto.response.LoginResponseDto;
import com.hanium.mom4u.global.security.service.NaverLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/naver")
@RequiredArgsConstructor
@Tag(name = "네이버 로그인 API Controller")
public class NaverLoginController {

    private final NaverLoginService naverLoginService;

    @Operation(summary = "네이버 로그인 URL 조회 API", description = """
            해당 URL로 이동시 네이버 서버로부터의 code와 state를 얻을 수 있습니다.
            """)
    @GetMapping
    public ResponseEntity<CommonResponse<?>> getUrl() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(naverLoginService.getCode())
        );
    }

    @Operation(summary = "네이버 로그인 API", description = """
            네이버 로그인 API입니다.
            Parameter로는 code와 state 파싱한 값을 입력해주세요.
            """)
    @ApiResponses(value = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<?>> naverLogin(
            HttpServletResponse response,
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(naverLoginService.loginWithNaver(response, code, state))
        );
    }
}
