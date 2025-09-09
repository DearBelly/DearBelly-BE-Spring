package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.dto.request.ProfileEditRequest;
import com.hanium.mom4u.domain.member.dto.response.MemberInfoResponse;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.external.s3.service.FileStorageService;
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


    @Transactional
    public void updateProfile(String nickname, Boolean isPregnant,
                              LocalDate LmpDate, Boolean prePregnant, Gender gender, LocalDate birth, Set<Category> categories) {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        member.setNickname(nickname);
        member.setPregnant(isPregnant);
        member.setPrePregnant(prePregnant);
        member.setGender(gender);
        member.setBirthDate(birth);

        // LMP 저장 로직을 수정했습니다.
        if (LmpDate != null) {
            if (member.getFamily() != null) {
                // 가족이 있으면 Family 객체에 LMP를 저장
                member.getFamily().setLmpDate(LmpDate);
                member.setLmpDate(null);
            } else {
                // 가족이 없으면 Member 객체에 직접 LMP를 저장
                member.setLmpDate(LmpDate);
            }
        } else {
            // LMP가 null로 들어오면 Member의 LMP도 null로 설정
            member.setLmpDate(null);
        }

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

        if (request.getLmpDate() != null) {
            if (member.getFamily() != null) {
                // 가족이 있으면 LMP를 Family 객체에 저장
                member.getFamily().setLmpDate(request.getLmpDate());
                // 기존 Member 객체의 LMP는 null로 설정 (중복 방지)
                member.setLmpDate(null);
            } else {
                // 가족이 없으면 LMP를 Member 객체에 직접 저장
                member.setLmpDate(request.getLmpDate());
            }
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

        LocalDate effectiveLmpDate = member.getFamily() != null ? member.getFamily().getLmpDate() : member.getLmpDate();

        return new MemberInfoResponse(
                member.getNickname(),
                member.getEmail(),
                imageUrl,
                member.isPregnant(),
                effectiveLmpDate,
                member.getPrePregnant(),
                member.getGender() == null ? null : member.getGender().name(),
                member.getBirthDate(),
                member.getInterests(),
                member.getSocialType().name()
        );
    }

    public void updateCategories(Set<Category> categories) {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        member.getInterests().clear();
        if (categories != null && !categories.isEmpty()) {
            member.getInterests().addAll(categories);
        }
        memberRepository.save(member);
    }

}
