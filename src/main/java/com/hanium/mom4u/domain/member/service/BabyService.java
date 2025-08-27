package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import com.hanium.mom4u.domain.member.dto.response.BabyInfoResponseDto;
import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BabyService {

    private final AuthenticatedProvider authenticatedProvider;

    private final BabyRepository babyRepository;
    private final MemberRepository memberRepository;


    // 태아 정보 등록하기
    @Transactional
    public BabyInfoResponseDto saveBaby(BabyInfoRequestDto requestDto) {

        Member member = authenticatedProvider.getCurrentMember();
        log.info("member 조회 완료: {}", member.getId());

        // 임산부인 경우에만 태아 정보 등록 가능
        if (member.isPregnant()) {
            log.info("임산부인 사용자의 태아 등록 시작...");
            Baby baby = Baby.builder()
                    .name(requestDto.getName())
                    .babyGender(requestDto.getBabyGender())
                    .lmpDate(requestDto.getLmpDate())
                    .isEnded(false)
                    .build();
            baby.setMember(member);
            babyRepository.save(baby);
            log.info("Baby ID {} 저장 성공...", baby.getId());

            return BabyInfoResponseDto.builder()
                    .babyId(baby.getId())
                    .name(baby.getName())
                    .lmpDate(requestDto.getLmpDate())
                    .babyGender(baby.getBabyGender())
                    .currentWeek(baby.getCurrentWeek())
                    .dueDateCalculated(baby.getDueDateCalculated())
                    .build();
        } else {
            log.warn("임산부가 아닙니다.");
            throw BusinessException.of(StatusCode.ONLY_PREGNANT);
        }
    }

    // 특정 태아 정보 조회하기
    @Transactional(readOnly = true)
    public BabyInfoResponseDto readBabyInfo(Long babyId) {
        Member me = authenticatedProvider.getCurrentMember();
        log.info("member 조회 완료: {}", me.getId());

        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> GeneralException.of(StatusCode.BABY_NOT_FOUND));

        // 권한: 본인 or 같은 가족이면 허용
        boolean sameOwner = baby.getMember().getId().equals(me.getId());
        boolean sameFamily = (baby.getMember().getFamily() != null && me.getFamily() != null &&
                baby.getMember().getFamily().getId().equals(me.getFamily().getId()));

        if (!(sameOwner || sameFamily)) {
            throw GeneralException.of(StatusCode.NOT_IN_FAMILY);
        }

        return BabyInfoResponseDto.builder()
                .babyId(baby.getId())
                .name(baby.getName())
                .babyGender(baby.getBabyGender())
                .lmpDate(baby.getLmpDate())
                .currentWeek(baby.getCurrentWeek())
                .dueDateCalculated(baby.getDueDateCalculated())
                .build();
    }

    // 등록된 태아 전체 조회하기
    @Transactional(readOnly = true)
    public List<BabyInfoResponseDto> readAllBabyInfo() {
        Member member = authenticatedProvider.getCurrentMember();
        log.info("member 조회 완료: {}", member.getId());

        if (member.isPregnant()) {
            return member.getBabyList()
                    .stream()
                    .map(baby ->
                        BabyInfoResponseDto.builder()
                                .babyId(baby.getId())
                                .name(baby.getName())
                                .babyGender(baby.getBabyGender())
                                .lmpDate(baby.getLmpDate())
                                .currentWeek(baby.getCurrentWeek())
                                .dueDateCalculated(baby.getDueDateCalculated())
                                .build())
                    .toList();
        } else{
            // 가족이 없으면 빈 배열 반환
            if (member.getFamily() == null) {
                log.info("가족 없음 → 빈 배열 반환");
                return List.of();
            }

            // 임산부가 아닐 때의 태아 조회
            List<Member> familyMembers = memberRepository.findByFamily(member.getFamily());
            log.info("{} family ID에 등록된 태아 조회...", member.getFamily().getId());
            return familyMembers.stream()
                    .filter(Member::isPregnant)
                    .flatMap(pregnantMember -> pregnantMember.getBabyList().stream())
                    .map(baby -> BabyInfoResponseDto.builder()
                            .babyId(baby.getId())
                            .name(baby.getName())
                            .babyGender(baby.getBabyGender())
                            .lmpDate(baby.getLmpDate())
                            .currentWeek(baby.getCurrentWeek())
                            .dueDateCalculated(baby.getDueDateCalculated())
                            .build())
                    .toList();
        }
    }

//    // 태아 정보 수정하기
    @Transactional
    public BabyInfoResponseDto updateBaby(Long babyId, BabyInfoRequestDto requestDto) {

        Member member = authenticatedProvider.getCurrentMember();
        log.info("member 조회 완료: {}", member.getId());

        // 임산부에게만 태아 정보 수정 권한 부여
        if (member.isPregnant()) {
            Baby baby = babyRepository.findById(babyId)
                    .map(b -> b.updateInfo(requestDto))
                    .orElseThrow(() -> GeneralException.of(StatusCode.BABY_NOT_FOUND));

            return BabyInfoResponseDto.builder()
                    .babyId(baby.getId())
                    .name(baby.getName())
                    .babyGender(baby.getBabyGender())
                    .lmpDate(baby.getLmpDate())
                    .currentWeek(baby.getCurrentWeek())
                    .dueDateCalculated(baby.getDueDateCalculated())
                    .build();
        } else {
            throw BusinessException.of(StatusCode.ONLY_PREGNANT);
        }
    }

    // 태아 정보 삭제하기
    @Transactional
    public void deleteBaby(Long babyId) {
        Member member = authenticatedProvider.getCurrentMember();
        log.info("member 조회 완료: {}", member.getId());

        // 임산부에게만 태아 정보 수정 권한 부여
        if (member.isPregnant()) {
            babyRepository.deleteById(babyId);
        } else {
            throw BusinessException.of(StatusCode.ONLY_PREGNANT);
        }
    }
}
