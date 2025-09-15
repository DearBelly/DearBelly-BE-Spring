package com.hanium.mom4u.domain.family.controller;

import com.hanium.mom4u.domain.family.dto.request.FamilyCodeRequest;
import com.hanium.mom4u.domain.family.dto.response.FamilyMemberResponse;
import com.hanium.mom4u.domain.family.service.FamilyService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "가족 코드 API", description = "가족 코드 생성/참여/조회/수정 관련 API입니다.")
public class FamilyController {
    private final FamilyService familyService;

    @Operation(summary = "가족 코드 생성 API", description = "임산부만 호출할 수 있으며, 3분 유효한 공유 코드를 생성합니다.")
    @PostMapping("/api/v1/family-code")
    public ResponseEntity<CommonResponse> generateFamilyCode() {
        String code = familyService.createFamilyCode();
        return ResponseEntity.ok(CommonResponse.onSuccess(code));
    }

    @Operation(summary = "가족 참여 API", description = "가족 코드를 입력해 3분 내 공유에 참여합니다.")
    @PostMapping("/api/v1/family-code/join")
    public ResponseEntity<CommonResponse> joinFamily(@RequestBody FamilyCodeRequest request){
        familyService.joinFamily(request.getCode());
        return ResponseEntity.ok(CommonResponse.onSuccess("참여 성공"));
    }

    @Operation(summary = "가족 코드 유효성 확인 + 사용자 목록 조회", description = "가족 코드가 유효한 경우 사용자 목록 반환. 유효하지 않으면 예외 발생")
    @GetMapping("/api/v1/family-code/members")
    public ResponseEntity<CommonResponse> getFamilyMembers() {
        List<FamilyMemberResponse> members = familyService.getFamilyMembersByFamily();
        return ResponseEntity.ok(CommonResponse.onSuccess(members));
    }

}
