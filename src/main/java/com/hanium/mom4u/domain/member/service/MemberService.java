package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.dto.request.ProfileEditRequest;
import com.hanium.mom4u.domain.member.dto.response.MemberInfoResponse;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final AuthenticatedProvider authenticatedProvider;
    private final FileStorageService fileStorageService;
    private final MemberRepository memberRepository;

    @Value("${spring.cloud.aws.s3.default-image}")
    private String DEFAULT_PROFILE_IMG_URL;

    public void updateProfile(String nickname, Boolean isPregnant,
                              LocalDate dueDate, Boolean prePregnant, String gender, LocalDate birth) {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        member.setNickname(nickname);
        member.setPregnant(isPregnant);
        member.setDueDate(dueDate);
        member.setPrePregnant(prePregnant);
        member.setGender(gender);
        member.setBirthDate(birth);

        if (member.getIsLightMode() == null) {
            member.setIsLightMode(true);
        }

        memberRepository.save(member);
    }

    public String getPresignedUploadUrl(String filename) {
        return fileStorageService.generatePresignedPutUrl("images/" + filename);
    }

    public void updateProfileImage(String imgUrl) {
        Member member = authenticatedProvider.getCurrentMember();
        member.setImgUrl(imgUrl);
        memberRepository.save(member);
    }

    public void editProfile(ProfileEditRequest request) {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        if (request.getNickname() != null) {
            member.setNickname(request.getNickname());
        }

        if (request.getDueDate() != null) {
            member.setDueDate(request.getDueDate());
        }

        if (request.getImgUrl() != null) {
            if (request.getImgUrl().isBlank()) {
                // 빈 문자열이면 기본 이미지로 설정
                member.setImgUrl(DEFAULT_PROFILE_IMG_URL);
            } else {
                // 새 이미지로 설정
                member.setImgUrl(request.getImgUrl());
            }
        }

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMyProfile() {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        String imageUrl = member.getImgUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = DEFAULT_PROFILE_IMG_URL;
        }

        return new MemberInfoResponse(
                member.getNickname(),
                member.getEmail(),
                imageUrl, // 수정된 이미지 URL 사용
                member.isPregnant(),
                member.getDueDate(),
                member.getPrePregnant(),
                member.getGender(),
                member.getBirthDate(),
                member.getSocialType().name()
        );
    }
}
