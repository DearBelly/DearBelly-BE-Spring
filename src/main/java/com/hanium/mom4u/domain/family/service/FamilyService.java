package com.hanium.mom4u.domain.family.service;

import com.hanium.mom4u.domain.family.dto.response.FamilyMemberResponse;
import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.family.repository.FamilyRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FamilyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AuthenticatedProvider authenticatedProvider;
    private final MemberRepository memberRepository;

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

        String code;
        do {
            code = generateRandomCode();  // ← UUID 대신
        } while (Boolean.TRUE.equals(redisTemplate.hasKey("FAMILY_CODE:" + code)));

        redisTemplate.opsForList().rightPush("FAMILY_CODE:" + code, member.getId().toString());
        redisTemplate.expire("FAMILY_CODE:" + code, CODE_EXPIRATION);

        return code;
    }


    // 코드로 참여
    public void joinFamily(String code) {
        Member member = authenticatedProvider.getCurrentMember();

        String key = "FAMILY_CODE:" + code;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE); // 커스텀 예외
        }

        List<String> userList = redisTemplate.opsForList().range(key, 0, -1);
        if (userList.contains(member.getId().toString())) {
            // 이미 등록된 사용자
            return;
        }

        redisTemplate.opsForList().rightPush(key, member.getId().toString());
    }

    // 사용자가 해당 코드 그룹에 속해 있는지 검증
    public boolean hasAccessToCode(String code) {
        Member member = authenticatedProvider.getCurrentMember();
        String key = "FAMILY_CODE:" + code;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) return false;

        List<String> userList = redisTemplate.opsForList().range(key, 0, -1);
        return userList.contains(member.getId().toString());
    }

    //코드 유효성
    public List<FamilyMemberResponse> getFamilyMembersByCode(String code) {
        String key = "FAMILY_CODE:" + code;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE);
        }

        List<String> userIdList = redisTemplate.opsForList().range(key, 0, -1);

        return userIdList.stream()
                .map(Long::parseLong)
                .map(memberRepository::findById)
                .flatMap(Optional::stream)
                .map(member -> FamilyMemberResponse.builder()
                        .nickname(member.getNickname())
                        .imgUrl(member.getImgUrl())
                        .isPregnant(member.isPregnant())
                        .build())
                .toList();
    }




}
