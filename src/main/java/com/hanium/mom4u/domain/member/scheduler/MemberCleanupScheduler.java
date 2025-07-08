package com.hanium.mom4u.domain.member.scheduler;

import com.hanium.mom4u.domain.calendar.repository.ScheduleRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final BabyRepository babyRepository;

    // 매일 12시 정각 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteInactiveMembersAfter7Days() {

        LocalDate deadline = LocalDate.now().minusDays(7);
        List<Member> expiredMembers = memberRepository.findAllByIsInactiveTrueAndInactiveDateBefore(deadline);

        if (expiredMembers.isEmpty()) {
            log.info("🟢 탈퇴 7일 지난 회원 없음.");
            return;
        }

        for (Member member : expiredMembers) {
            log.info("💀 회원 ID {} 의 연관 데이터를 먼저 삭제합니다.", member.getId());

            // 연관 데이터 수동 삭제
            babyRepository.deleteByMember(member);
            scheduleRepository.deleteByMember(member);

            // 회원 삭제
            memberRepository.delete(member);
            log.info("✅ 회원 ID {} 하드 삭제 완료.", member.getId());
        }

        log.info("🧹 총 {}명의 탈퇴 회원 하드 삭제 완료.", expiredMembers.size());
    }
}
