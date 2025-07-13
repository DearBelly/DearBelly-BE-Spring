package com.hanium.mom4u.global.security.controller;

import com.hanium.mom4u.global.response.CommonResponse;
import com.hanium.mom4u.global.security.dto.response.KakaoLoginResult;
import com.hanium.mom4u.global.security.dto.response.LoginResponseDto;
import com.hanium.mom4u.global.security.service.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "카카오 로그인 API Controller")
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;

    @Operation(
            summary = "카카오 로그인 URL 발급",
            description = "카카오 소셜 로그인을 위한 인가 코드 발급 URL을 CommonResponse 형태로 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카카오 로그인 URL 반환 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                                {
                                                  "httpStatus": 200,
                                                  "message": "success",
                                                  "data": "https://kauth.kakao.com/oauth/authorize?client_id=...",
                                                  "success": true
                                                }
                                            """))
                    )
            }
    )
    @GetMapping("/url/kakao")
    public CommonResponse<String> getKakaoLoginUrl() {
        String url = kakaoLoginService.getLoginUrl();
        return CommonResponse.onSuccess(url);
    }


    @Operation(
            summary = "카카오 로그인 처리",
            description = "카카오 인가 코드(code)를 받아 JWT 토큰을 포함한 로그인 결과를 CommonResponse 형태로 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카카오 로그인 성공",
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "비즈니스 실패(예: 인가 코드 만료)",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                                {
                                                  "httpStatus": 200,
                                                  "message": "카카오 인가 코드가 이미 사용되었거나 만료되었습니다.",
                                                  "data": null,
                                                  "success": false
                                                }
                                            """))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 에러",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                                {
                                                  "httpStatus": "INTERNAL_SERVER_ERROR",
                                                  "code": "SERVER5001",
                                                  "message": "서버에서 에러가 발생했습니다."
                                                }
                                            """))
                    )
            }
    )
    @PostMapping("/kakao")
    public CommonResponse<LoginResponseDto> kakaoLogin(@RequestParam String code, HttpServletResponse response) {
        KakaoLoginResult result = kakaoLoginService.login(response, code);
        return CommonResponse.onSuccess(result.getUserInfo());
    }
}


