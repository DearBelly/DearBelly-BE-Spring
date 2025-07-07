package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.dto.response.MemberInfoResponse;
import com.hanium.mom4u.domain.member.dto.response.ThemeResponse;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final AuthenticatedProvider authenticatedProvider;
    private final FileStorageService fileStorageService;
    private final MemberRepository memberRepository;
    private static final String DEFAULT_PROFILE_IMG_URL = "/images/default-profile.png";

    public void updateProfile(String nickname, MultipartFile imgFile,Boolean isPregnant,
                              LocalDate dueDate, Boolean prePregnant, String gender, LocalDate birth){
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        member.setNickname(nickname);


        if(imgFile != null && !imgFile.isEmpty()){
            String imgUrl = fileStorageService.save(imgFile);
            member.setImgUrl(imgUrl);
        } else {
            member.setImgUrl(DEFAULT_PROFILE_IMG_URL);
        }

        member.setPregnant(isPregnant);
        member.setDueDate(dueDate); // Member 엔티티에 dueDate 필드 추가 필요
        member.setPrePregnant(prePregnant); // Member 엔티티에 prePregnant 필드 추가 필요
        member.setGender(gender);
        member.setBirthDate(birth); // Member 엔티티에 birth 필드 추가 필요
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMyProfile() {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        return new MemberInfoResponse(
                member.getNickname(),
                member.getEmail(),
                member.getImgUrl(),
                member.isPregnant(),
                member.getDueDate(),
                member.getPrePregnant(),
                member.getGender(),
                member.getBirthDate(),
                member.getSocialType().name()
        );
    }



}

