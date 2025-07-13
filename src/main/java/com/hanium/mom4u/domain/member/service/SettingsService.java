package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.dto.response.ThemeResponse;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final AuthenticatedProvider authenticatedProvider;
    private final MemberRepository memberRepository;

    public Boolean getLightMode() {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
        Boolean isLightMode = member.getIsLightMode();
        return isLightMode != null ? isLightMode : true;
    }


    @Transactional
    public void updateLightMode(Boolean isLightMode) {
        Member member = authenticatedProvider.getCurrentMember();
        member = memberRepository.findById(member.getId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
        member.setIsLightMode(isLightMode);
        memberRepository.save(member);
    }

}


