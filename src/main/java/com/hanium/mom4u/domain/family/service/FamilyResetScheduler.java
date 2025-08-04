package com.hanium.mom4u.domain.family.service;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.family.repository.FamilyRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FamilyResetScheduler {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    // 실제 운영용: 매일 자정(00:00) 실행
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetOldFamilies() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3); // 3달마다 가족 초기화
        List<Family> oldFamilies = familyRepository.findAll().stream()
                .filter(f -> f.getCreatedAt().isBefore(threeMonthsAgo))
                .toList();

        for (Family family : oldFamilies) {
            for (Member member : family.getMemberList()) {
                member.assignFamily(family);
                memberRepository.save(member);
            }
            log.info("[운영] 3개월 경과로 초기화된 가족 ID: {}", family.getId());
        }
    }
}