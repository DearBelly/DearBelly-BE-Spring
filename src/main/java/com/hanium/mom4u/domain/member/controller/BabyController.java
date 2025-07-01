package com.hanium.mom4u.domain.member.controller;

import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import com.hanium.mom4u.domain.member.service.BabyService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/baby")
@RequiredArgsConstructor
@Tag(name = "태아 정보 API Controller", description = "태아 정보 관련 API Controller입니다.")
public class BabyController {

    private final BabyService babyService;

    @Operation(summary = "태아 정보 저장 API", description = """
            태아의 정보를 저장하는 API입니다.<br>
            임산부가 아닐 경우 저장이 불가능합니다.
            """)
    @PostMapping("")
    public ResponseEntity<CommonResponse<?>> saveBaby(
            @RequestBody BabyInfoRequestDto requestDto) {

        return ResponseEntity.ok(
                CommonResponse.onSuccess(babyService.saveBaby(requestDto))
        );
    }

    @Operation(summary = "태아 정보 수정 API", description = """
            태아의 정보를 수정하는 API입니다.<br>
            임산부가 아닐 경우 수정이 불가능합니다.
            """)
    @PatchMapping("/{babyId}")
    public ResponseEntity<CommonResponse<?>> updateBaby(
            @PathVariable("babyId") Long babyId,
            @RequestBody BabyInfoRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(babyService.updateBaby(babyId, requestDto))
        );
    }

    @Operation(summary = "등록된 태아 정보 전체 조회 API", description = """
            가족 구성원 간에 등록된 태아 전체를 조회합니다.<br>
            """)
    @GetMapping("")
    public ResponseEntity<CommonResponse<?>> getBabyAll() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(babyService.readAllBabyInfo())
        );
    }

    @Operation(summary = "특정 태아 정보 조회하기 API", description = """
            특정 태아의 정보만 조회하는 API입니다.<br>
            """)
    @GetMapping("/{babyId}")
    public ResponseEntity<CommonResponse<?>> getBabyById(@PathVariable("babyId") Long babyId) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(babyService.readBabyInfo(babyId))
        );
    }

    @Operation(summary = "특정 태아의 정보 삭제 API", description = """
            특정 태아를 삭제하는 API입니다.<br>
            임산부가 아닐 경우 삭제가 불가능합니다.
            """)
    @DeleteMapping("/{babyId}")
    public ResponseEntity<CommonResponse<Void>> deleteBaby(@PathVariable("babyId") Long babyId) {

        babyService.deleteBaby(babyId);
        return ResponseEntity.ok(
                CommonResponse.onSuccess()
        );
    }
}
