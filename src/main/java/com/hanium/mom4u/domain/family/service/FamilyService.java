package com.hanium.mom4u.domain.family.service;

import com.hanium.mom4u.domain.family.dto.response.FamilyMemberResponse;
import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.family.repository.FamilyRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FamilyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AuthenticatedProvider authenticatedProvider;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;

    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(3);


    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 10;

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CODE_CHARACTERS.length());
            code.append(CODE_CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    // 코드 생성
    public String createFamilyCode() {
        Member member = authenticatedProvider.getCurrentMember();

        if (!member.isPregnant()) {
            throw new BusinessException(StatusCode.FORBIDDEN_FAMILY_CODE_CREATION);
        }

        Family family;
        // 이미 가족이 있는 경우 그 가족 재사용
        if (member.getFamily() != null) {
            family = member.getFamily();
        } else {
            family = new Family();
            familyRepository.save(family);
            member.assignFamily(family);
            memberRepository.save(member);
        }

        // 코드 생성
        String code;
        do {
            code = generateRandomCode();
        } while (Boolean.TRUE.equals(redisTemplate.hasKey("FAMILY_CODE:" + code)));

        redisTemplate.opsForList().rightPush("FAMILY_CODE:" + code, member.getId().toString());
        redisTemplate.expire("FAMILY_CODE:" + code, CODE_EXPIRATION);

        return code;
    }


    // 코드로 참여
    @Transactional
    public void joinFamily(String code) {
        Member member = authenticatedProvider.getCurrentMember();

        // 이미 가족이 있는 경우 참여 불가
        if (member.getFamily() != null) {
            throw new BusinessException(StatusCode.ALREADY_IN_FAMILY);
        }

        String key = "FAMILY_CODE:" + code;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE);
        }

        List<String> userList = redisTemplate.opsForList().range(key, 0, -1);
        if (userList == null || userList.isEmpty()) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE);
        }

        if (!userList.contains(member.getId().toString())) {
            redisTemplate.opsForList().rightPush(key, member.getId().toString());
        }

        // 임산부 ID로부터 가족 조회
        Long pregnantMemberId = Long.parseLong(userList.get(0));
        Family family = memberRepository.findById(pregnantMemberId)
                .flatMap(m -> Optional.ofNullable(m.getFamily()))
                .orElseThrow(() -> new BusinessException(StatusCode.UNREGISTERED_FAMILY));

        member.assignFamily(family);
        memberRepository.save(member);
    }


    //코드 유효성
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getFamilyMembersByFamily() {
        Member member = authenticatedProvider.getCurrentMember();
        Family family = member.getFamily();

        if (family == null) {
            throw new BusinessException(StatusCode.NOT_IN_FAMILY); // 가족에 속해있지 않은 경우
        }

        return family.getMemberList().stream()
                .map(m -> FamilyMemberResponse.builder()
                        .nickname(m.getNickname())
                        .imgUrl(m.getImgUrl())
                        .isPregnant(m.isPregnant())
                        .build())
                .toList();
    }


}
