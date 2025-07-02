package com.hanium.mom4u.domain.family.controller;

import com.hanium.mom4u.domain.family.dto.request.FamilyCodeRequest;
import com.hanium.mom4u.domain.family.service.FamilyService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "가족 코드 API", description = "가족 코드 생성/참여/조회/수정 관련 API입니다.")
public class FamilyController {
    private final FamilyService familyService;

    @Operation(summary = "가족 코드 생성 API(마이페이지)", description = """
            임산부만 호출 가능한 API입니다.<br>
            랜덤한 8자리 코드가 자동으로 생성되며, 기존 가족이 없을 경우 새 가족이 생성됩니다.<br>
            기존 가족이 있을 경우에는 가족 코드만 갱신됩니다.
            """)
    @PostMapping("/api/v1/family-code")
    public ResponseEntity<CommonResponse> generateFamilyCode() {
        String code = familyService.createFamilyCode(); // 파라미터 없음
        return ResponseEntity.ok(CommonResponse.onSuccess(code));
    }

    @Operation(summary = "가족 참여 API", description = """
            보호자 또는 가족 구성원이 가족 코드를 입력해 가족에 참여하는 API입니다.<br>
            이미 가족에 소속된 사용자는 참여할 수 없습니다.<br>
            유효하지 않거나 만료된 코드 입력 시 예외가 발생합니다.
            """)
    @PostMapping("api/v1/family-code/join")
    public ResponseEntity<CommonResponse> joinFamily(@RequestBody FamilyCodeRequest request){
        familyService.joinFamily(request.getCode());
        return ResponseEntity.ok(CommonResponse.onSuccess("가족 참여 완료"));
    }

    @Operation(summary = "가족 코드 조회 API", description = """
            현재 로그인한 사용자의 가족 코드 정보를 조회하는 API입니다.<br>
            가족에 소속되어 있지 않으면 예외가 발생합니다.
            """)
    @GetMapping("/api/v1/family-code")
    public ResponseEntity<CommonResponse> getFamilyCode() {
        String code = familyService.getFamilyCode();
        return ResponseEntity.ok(CommonResponse.onSuccess(code));
    }

    @Operation(summary = "가족 코드 재발급 API", description = """
            임산부만 호출 가능한 API입니다.<br>
            기존 코드가 만료되고 새로 랜덤한 8자리 코드로 재발급됩니다.<br>
            이전 코드로는 가족 참여가 불가능합니다.
            """)
    @PatchMapping("/api/v1/family-code")
    public ResponseEntity<CommonResponse> updateFamilyCode() {
        String updatedCode = familyService.createNewFamilyCode();  // 새 코드 자동 생성
        return ResponseEntity.ok(CommonResponse.onSuccess(updatedCode));
    }





}
