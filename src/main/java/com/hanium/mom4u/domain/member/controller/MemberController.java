package com.hanium.mom4u.domain.member.controller;

import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.dto.request.CategoryUpdateRequest;
import com.hanium.mom4u.domain.member.dto.request.ImageCommitRequest;
import com.hanium.mom4u.domain.member.dto.request.ProfileEditRequest;
import com.hanium.mom4u.domain.member.dto.response.UploadUrlResponse;
import com.hanium.mom4u.domain.member.service.MemberService;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "회원 프로필 API", description = "닉네임/프로필 사진 변경 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/profile")
    @Operation(summary = "회원정보 등록", description = "닉네임, 임신 상태, 출산 예정일 등 기본 정보를 등록합니다.")
    public ResponseEntity<CommonResponse<Void>> updateProfile(
            @RequestParam("nickname") String nickname,
            @RequestParam("isPregnant") Boolean isPregnant,
            @RequestParam(value = "lmpDate", required = false)
            @DateTimeFormat(pattern = "yyyy.MM.dd") LocalDate lmpDate,
            @RequestParam(value = "pre_pregnant", required = false) Boolean prePregnant,
            @RequestParam("gender") Gender gender,
            @RequestParam("birth")
            @DateTimeFormat(pattern = "yyyy.MM.dd")LocalDate birth,
            @RequestParam(value = "categories", required = false) Set<Category> categories
    ) {
        memberService.updateProfile(nickname, isPregnant, lmpDate, prePregnant, gender, birth, categories);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }
    @Operation(summary = "프로필 이미지 업로드용 Presigned URL 발급",
            description = "S3에 업로드할 PUT presigned URL과 objectKey를 발급합니다.")
    @GetMapping("/profile/upload-url")
    public ResponseEntity<CommonResponse<UploadUrlResponse>> getPresignedUploadUrl(@RequestParam String filename) {
        var res = memberService.issuePresignedPut(filename);
        return ResponseEntity.ok(CommonResponse.onSuccess(res));
    }
    @Operation(summary = "프로필 이미지 저장",
            description = """
    S3 업로드가 끝난 `objectKey`를 전달하면 서버가 **공개 URL을 확정**하고 **DB에 저장**합니다.<br>
    - 먼저 `/profile/upload-url`로 `putUrl/objectKey`를 발급받고,<br>
    - `putUrl`로 S3에 파일을 **PUT 업로드**,<br>
    - 마지막으로 본 API에 아래 **Request Body**를 보내세요.
    """)
    @PostMapping("/profile/image/commit")
    public ResponseEntity<CommonResponse<Void>> commitProfileImage(@RequestBody ImageCommitRequest req) {
        memberService.commitProfileImage(req.objectKey());
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "프로필 이미지 기본값으로 리셋",
            description = "업로드 없이 기본 이미지로 되돌립니다.")
    @DeleteMapping("/profile/image")
    public ResponseEntity<CommonResponse<Void>> resetProfileImage() {
        memberService.resetProfileImageToDefault();
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "회원 닉네임, 출산예정일 수정", description = "마이페이지에서 닉네임, 출산 예정일을 수정합니다.")
    @PatchMapping("/profile/edit")
    public ResponseEntity<CommonResponse<Void>> editProfile(@RequestBody ProfileEditRequest request) {
        memberService.editProfile(request);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }

    @Operation(summary = "회원정보 조회", description = "현재 로그인한 회원 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getMyProfile() {
        return ResponseEntity.ok(CommonResponse.onSuccess(memberService.getMyProfile()));
    }

    @Operation(summary = "관심 카테고리 변경(단독)", description = "다른 필드 건드리지 않고 관심 카테고리만 교체합니다.")
    @PatchMapping("/profile/categories")
    public ResponseEntity<CommonResponse<Void>> updateCategories(@RequestBody CategoryUpdateRequest req) {
        memberService.updateCategories(req.getInterests());
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }
}

