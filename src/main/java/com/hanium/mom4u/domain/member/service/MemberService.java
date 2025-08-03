package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.dto.request.ProfileEditRequest;
import com.hanium.mom4u.domain.member.dto.response.MemberInfoResponse;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

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
                              LocalDate dueDate, Boolean prePregnant, String gender, LocalDate birth, Set<Category> categories) {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        member.setNickname(nickname);
        member.setPregnant(isPregnant);
        member.setDueDate(dueDate);
        member.setPrePregnant(prePregnant);
        member.setGender(gender);
        member.setBirthDate(birth);
        member.getInterests().addAll(categories);



        memberRepository.save(member);
    }

    public String getPresignedUploadUrl(String filename) {
        Member member = authenticatedProvider.getCurrentMember();
        String objectKey = "images/" + member.getId() + "/" + filename;
        return fileStorageService.generatePresignedPutUrl(objectKey);
    }

    //기본 이미지가 아닌 경우 S3에서 삭제
    private void deleteIfCustomImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()
                && !imageUrl.equals(DEFAULT_PROFILE_IMG_URL)) {
            String objectKey = extractKeyFromUrl(imageUrl);
            fileStorageService.deleteFile(objectKey);
        }
    }


    public void updateProfileImage(String newImgUrl) {
        Member member = authenticatedProvider.getCurrentMember();
        String oldImgUrl = member.getImgUrl();


        deleteIfCustomImage(oldImgUrl);


        member.setImgUrl(newImgUrl);
        memberRepository.save(member);
    }

    private String extractKeyFromUrl(String fullUrl) {
        int index = fullUrl.indexOf(".amazonaws.com/");
        if (index == -1) return ""; // 예외 처리
        return fullUrl.substring(index + ".amazonaws.com/".length());
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
            deleteIfCustomImage(member.getImgUrl());

            // 새로운 값 적용
            if (request.getImgUrl().isBlank()) {
                member.setImgUrl(DEFAULT_PROFILE_IMG_URL);
            } else {
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
                imageUrl,
                member.isPregnant(),
                member.getDueDate(),
                member.getPrePregnant(),
                member.getGender(),
                member.getBirthDate(),
                member.getInterests(),
                member.getSocialType().name()
        );
    }
}
