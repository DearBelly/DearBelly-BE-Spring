package com.hanium.mom4u.domain.member.controller;

import com.hanium.mom4u.domain.member.service.MemberService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Tag(name = "회원 프로필 API", description = "닉네임/프로필 사진 변경 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입: 개인정보 입력", description = "회원정보를 입력합니다.")
    @PatchMapping("/profile")
    public ResponseEntity<CommonResponse<Void>> updateProfile(
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "imgFile", required = false) MultipartFile imgFile,
            @RequestParam("isPregnant") Boolean isPregnant,
            @RequestParam(value = "dueDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(value = "pre_pregnant", required = false) Boolean prePregnant,
            @RequestParam("gender") String gender,
            @RequestParam("birth")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birth
    )
{
        memberService.updateProfile(nickname,imgFile,isPregnant,dueDate,prePregnant,gender,birth);
        return ResponseEntity.ok(CommonResponse.onSuccess());
    }


    @Operation(summary = "회원정보 조회", description = "현재 로그인한 회원 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse> getMyProfile() {
        return ResponseEntity.ok(CommonResponse.onSuccess(memberService.getMyProfile()));
    }


}

