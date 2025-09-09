package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import com.hanium.mom4u.domain.member.dto.response.BabyInfoResponseDto;
import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BabyService {

    private final AuthenticatedProvider authenticatedProvider;
    private final BabyRepository babyRepository;
    private final MemberRepository memberRepository;

    private boolean sameFamily(Member a, Member b) {
        return a.getFamily() != null && b.getFamily() != null && a.getFamily().getId().equals(b.getFamily().getId());
    }

    private boolean canManage(Member actor, Member owner) {
        return actor.getId().equals(owner.getId()) || sameFamily(actor, owner);
    }

    // 가족 여부에 따라 LMP 날짜를 가져오는 헬퍼 메서드
    private LocalDate getLmpDateForMember(Member member) {
        if (member.getFamily() != null) {
            return member.getFamily().getLmpDate();
        }
        return member.getLmpDate();
    }

    // 태아 정보 등록하기
    @Transactional
    public BabyInfoResponseDto saveBabyFor(Long targetMemberId, BabyInfoRequestDto requestDto) {
        Member actor = authenticatedProvider.getCurrentMember();
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        if (!canManage(actor, target)) {
            throw GeneralException.of(StatusCode.NOT_IN_FAMILY);
        }

        Baby baby = Baby.builder()
                .name(requestDto.getName())
                .babyGender(requestDto.getBabyGender())
                .isEnded(false)
                .build();
        baby.setMember(target);
        babyRepository.save(baby);

        // LMP를 가져와 DTO 생성 전에 아기 객체에 설정
        LocalDate lmpDate = getLmpDateForMember(target);
        baby.setLmpDate(lmpDate);

        return BabyInfoResponseDto.builder()
                .babyId(baby.getId())
                .name(baby.getName())
                .lmpDate(baby.getLmpDate())
                .babyGender(baby.getBabyGender())
                .currentWeek(baby.getCurrentWeek())
                .dueDateCalculated(baby.getDueDateCalculated())
                .build();
    }

    @Transactional
    public BabyInfoResponseDto saveBaby(BabyInfoRequestDto requestDto) {
        Member me = authenticatedProvider.getCurrentMember();
        return saveBabyFor(me.getId(), requestDto);
    }

    // 특정 태아 정보 조회하기
    @Transactional(readOnly = true)
    public BabyInfoResponseDto readBabyInfo(Long babyId) {
        Member me = authenticatedProvider.getCurrentMember();
        log.info("member 조회 완료: {}", me.getId());

        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> GeneralException.of(StatusCode.BABY_NOT_FOUND));

        boolean sameOwner = baby.getMember().getId().equals(me.getId());
        boolean sameFamily = (baby.getMember().getFamily() != null && me.getFamily() != null &&
                baby.getMember().getFamily().getId().equals(me.getFamily().getId()));

        if (!(sameOwner || sameFamily)) {
            throw GeneralException.of(StatusCode.NOT_IN_FAMILY);
        }

        // LMP를 가져와 DTO 생성 전에 아기 객체에 설정
        LocalDate lmpDate = getLmpDateForMember(me);
        baby.setLmpDate(lmpDate);

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
        Member me = authenticatedProvider.getCurrentMember();
        // LMP는 가족 전체가 공유하므로, me 객체를 통해 한 번만 가져옵니다.
        LocalDate myLmpDate = getLmpDateForMember(me);

        if (me.getFamily() == null) {
            return me.getBabyList().stream().map(baby -> {
                baby.setLmpDate(myLmpDate);
                return BabyInfoResponseDto.builder()
                        .babyId(baby.getId())
                        .name(baby.getName())
                        .babyGender(baby.getBabyGender())
                        .lmpDate(baby.getLmpDate())
                        .currentWeek(baby.getCurrentWeek())
                        .dueDateCalculated(baby.getDueDateCalculated())
                        .build();
            }).toList();
        }

        List<Member> familyMembers = memberRepository.findByFamily(me.getFamily());
        return familyMembers.stream()
                .flatMap(m -> m.getBabyList().stream())
                .map(baby -> {
                    baby.setLmpDate(myLmpDate); // 💡 수정: myLmpDate를 사용합니다.
                    return BabyInfoResponseDto.builder()
                            .babyId(baby.getId())
                            .name(baby.getName())
                            .babyGender(baby.getBabyGender())
                            .lmpDate(baby.getLmpDate())
                            .currentWeek(baby.getCurrentWeek())
                            .dueDateCalculated(baby.getDueDateCalculated())
                            .build();
                })
                .toList();
    }

    // 태아 정보 수정하기
    @Transactional
    public BabyInfoResponseDto updateBaby(Long babyId, BabyInfoRequestDto requestDto) {
        Member me = authenticatedProvider.getCurrentMember();
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> GeneralException.of(StatusCode.BABY_NOT_FOUND));

        if (!canManage(me, baby.getMember())) {
            throw GeneralException.of(StatusCode.NOT_IN_FAMILY);
        }

        baby.updateInfo(requestDto);
        // LMP를 가져와 DTO 생성 전에 아기 객체에 설정
        LocalDate lmpDate = getLmpDateForMember(me);
        baby.setLmpDate(lmpDate);

        return BabyInfoResponseDto.builder()
                .babyId(baby.getId())
                .name(baby.getName())
                .babyGender(baby.getBabyGender())
                .lmpDate(baby.getLmpDate())
                .currentWeek(baby.getCurrentWeek())
                .dueDateCalculated(baby.getDueDateCalculated())
                .build();
    }

    // 태아 정보 삭제하기
    @Transactional
    public void deleteBaby(Long babyId) {
        Member me = authenticatedProvider.getCurrentMember();
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> GeneralException.of(StatusCode.BABY_NOT_FOUND));

        if (!canManage(me, baby.getMember())) {
            throw GeneralException.of(StatusCode.NOT_IN_FAMILY);
        }
        babyRepository.deleteById(babyId);
    }
}