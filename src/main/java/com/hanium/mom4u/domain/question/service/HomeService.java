package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.question.dto.response.HomeResponse;
import com.hanium.mom4u.domain.question.repository.LetterReadRepository;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final BabyRepository babyRepository;
    private final MemberRepository memberRepository;
    private final LetterReadRepository letterReadRepository;
    private final AuthenticatedProvider authenticatedProvider;

    /** 아기 이름, 주차(0부터), 편지 읽음 여부 */
    public HomeResponse getTopBanner() {
        Member me = authenticatedProvider.getCurrentMember();

        // 1) 임산부면 본인 진행 중 아기, 2) 아니면 가족 진행 중 아기
        java.util.Optional<Baby> babyOpt = me.isPregnant()
                ? babyRepository.findCurrentByMemberId(me.getId())
                : findFamilyOngoingBaby(me);

        boolean hasUnread = hasUnreadLetterIcon(me);

        //  아기 없으면 기본값으로 응답 (에러 X)
        if (babyOpt.isEmpty()) {
            return HomeResponse.builder()
                    .babyName(null)      // 프론트에서 placeholder 처리
                    .week(0)             // 0주차
                    .hasUnreadLetters(hasUnread)
                    .build();
        }

        Baby baby = babyOpt.get();
        String babyName = (baby.getName() == null || baby.getName().isBlank()) ? null : baby.getName();

        return HomeResponse.builder()
                .babyName(babyName)
                .week(baby.getCurrentWeek())     // LMP 기준 0주차부터
                .hasUnreadLetters(hasUnread)
                .build();
    }

    /** 같은 가족의 진행 중 아기 1건 Optional (가족 없으면 empty) */
    private java.util.Optional<Baby> findFamilyOngoingBaby(Member me) {
        if (me.getFamily() == null) return java.util.Optional.empty();
        return babyRepository.findCurrentByFamilyId(me.getFamily().getId());
    }


    private boolean hasUnreadLetterIcon(Member me) {
        if (me.getFamily() == null) return false;
        Long familyId = me.getFamily().getId();
        if (memberRepository.countByFamilyId(familyId) <= 1) return false;
        return letterReadRepository.countUnreadForMember(familyId, me.getId()) > 0;
    }
}
