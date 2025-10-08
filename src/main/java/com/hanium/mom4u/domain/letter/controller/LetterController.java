package com.hanium.mom4u.domain.letter.controller;

import com.hanium.mom4u.domain.letter.dto.request.LetterRequest;
import com.hanium.mom4u.domain.letter.dto.request.ThemeRequest;
import com.hanium.mom4u.domain.letter.dto.response.*;
import com.hanium.mom4u.domain.letter.service.LetterService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/letters")
@RequiredArgsConstructor
@Tag(name = "편지 API Controller")
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

    @Operation(summary = "오늘의 질문 + 내 오늘 편지 상태")
    @GetMapping("/today")
    public ResponseEntity<CommonResponse<TodayWriteResponse>> today() {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.getTodayForWrite()));
    }

    @Operation(summary = "편지 쓰기 API", description = "편지 작성 API입니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<Void>> create(@RequestBody @Valid LetterRequest request) {
        letterService.create(request);
        return ResponseEntity.ok(
                CommonResponse.onSuccess(
        ));
    }
    @Operation(summary = "가족 편지 피드(전체 기간) - 각 편지에 날짜별 질문 포함")
    @GetMapping("/feed")
    public ResponseEntity<CommonResponse<FeedResponse>> feed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.getFamilyFeed(cursor, size)));
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

    @Operation(summary = "홈 테마 조회 (비로그인/로그인 공통)", description = "쿠키 > 회원DB > 기본 MINT")
    @GetMapping("/theme")
    public ResponseEntity<CommonResponse<ThemeResponse>> getTheme(HttpServletRequest req) {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.getTheme(req)));
    }

    @Operation(summary = "홈 테마 변경 (비로그인=쿠키, 로그인=쿠키+DB)", description = "body.theme = SUNSET|MINT|COTTONLIGHT|COTTONDARK|NIGHT|VIOLA")
    @PutMapping("/theme")
    public ResponseEntity<CommonResponse<ThemeResponse>> updateTheme(
            @RequestBody @Valid ThemeRequest request,
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        return ResponseEntity.ok(CommonResponse.onSuccess(letterService.updateTheme(request.getTheme(), req, resp)));
    }

    @Operation(summary = "안 읽은 편지 확인을 위한 API", description = """
            읽지 않은 편지가 존재하는지에 대하여 확인합니다. 단순 존재 여부에 대한 DTO를 반환합니다. <br>
            isUnreadExist 필드가 true면 읽지 않은 게 존재, false면 읽지 않은 게 존재하지 않습니다. <br>
            편지 아이콘에 사용해주세요.<ㅠㄱ>
            """)
    @ApiResponse(responseCode = "200",
    content = @Content(
            schema = @Schema(implementation = LetterCheckResponseDto.class)
    ))
    @GetMapping("/check")
    public ResponseEntity<CommonResponse<?>> getUnCheckedLetters() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(
                        letterService.getLetterCheck()
                )
        );
    }
}


