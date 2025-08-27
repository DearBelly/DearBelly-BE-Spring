package com.hanium.mom4u.global.security.jwt;

import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedProvider {

    private final MemberRepository memberRepository;

    // 인증된 MemberId 반환
    public Long getCurrentMemberId() {
        return getCurrentMember().getId();
    }

    // 현재 인증된 Member를 반환
    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
            throw GeneralException.of(StatusCode.UNAUTHORIZED_ACCESS);
        }
        Long memberId = Long.parseLong(authentication.getName());
        return memberRepository
                .findById(memberId).orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
    }
}
