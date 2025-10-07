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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;



@Service
@Slf4j
@RequiredArgsConstructor
public class FamilyService {

    private final RedisTemplate<String, String> redisStringTemplate;
    private final AuthenticatedProvider authenticatedProvider;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;

    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(6);

    @Value("${spring.security.hmac.family-secret}")
    private String secretKey;
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 10;
    private static final String REDIS_KEY_PREFIX = "FAMILY_CODE:";


    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CODE_CHARACTERS.length());
            code.append(CODE_CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    private String createHmacSignature(String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new BusinessException(StatusCode.INTERNAL_SERVER_ERROR);
        }
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
            // LMP를 Member에서 Family로 옮기기
            family.setLmpDate(member.getLmpDate());
            familyRepository.save(family);
            family.addMember(member);
            member.setLmpDate(null);
            memberRepository.save(member);
        }

        // 1. Redis에 유효한 코드가 있는지 확인
        String existingCode = redisStringTemplate.opsForValue().get(REDIS_KEY_PREFIX + family.getId());
        if (existingCode != null) {
            // 2. 유효한 코드가 있으면 해당 코드 반환 (재사용)
            return existingCode;
        }


        // 코드 생성
        String code;
        String combinedCode;
        do {
            code = generateRandomCode();
            String hmac = createHmacSignature(code);
            combinedCode = code + "." + hmac;
        } while (Boolean.TRUE.equals(redisStringTemplate.hasKey(REDIS_KEY_PREFIX + combinedCode)));

        // 새로운 코드와 가족 ID를 Redis에 저장
        redisStringTemplate.opsForValue().set(REDIS_KEY_PREFIX + combinedCode, family.getId().toString(), CODE_EXPIRATION);

        // 가족 ID를 키로 하여 조합된 코드를 저장 (재사용을 위해)
        redisStringTemplate.opsForValue().set(REDIS_KEY_PREFIX + family.getId(), combinedCode, CODE_EXPIRATION);

        return combinedCode;
    }


    // 코드로 참여
    @Transactional
    public void joinFamily(String code) {
        Member member = authenticatedProvider.getCurrentMember();

        if (member.getFamily() != null) {
            throw new BusinessException(StatusCode.ALREADY_IN_FAMILY);
        }

        if (!code.contains(".")) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE);
        }
        String[] parts = code.split("\\.");
        String providedCode = parts[0];
        String providedHmac = parts[1];

        String expectedHmac = createHmacSignature(providedCode);
        if (!providedHmac.equals(expectedHmac)) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE);
        }

        String key = REDIS_KEY_PREFIX + code;
        String familyIdStr = redisStringTemplate.opsForValue().get(key);

        if (familyIdStr == null) {
            throw new BusinessException(StatusCode.INVALID_FAMILY_CODE);
        }

        Long familyId = Long.parseLong(familyIdStr);
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException(StatusCode.UNREGISTERED_FAMILY));

        member.setFamily(family);          // FK 세팅
        family.getMemberList().add(member);

        memberRepository.saveAndFlush(member);

        log.info("[joinFamily] member {} joined family {}", member.getId(), family.getId());

    }

    // 코드 유효성
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getFamilyMembersByFamily() {
        Member member = authenticatedProvider.getCurrentMember();
        Family family = member.getFamily();

        if (family == null) {
            throw new BusinessException(StatusCode.NOT_IN_FAMILY);
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

