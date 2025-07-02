package com.hanium.mom4u.domain.family.service;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.family.repository.FamilyRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.BusinessException;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final AuthenticatedProvider authenticatedProvider;
    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String createFamilyCode() {
        Member member = authenticatedProvider.getCurrentMember();

        if (!member.isPregnant()) {
            throw BusinessException.of(StatusCode.ONLY_PREGNANT);
        }

        // 랜덤 코드 생성 및 중복 방지
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (familyRepository.existsByCode(code));

        // 기존 가족이 없으면 새로 생성
        if (member.getFamily() == null) {
            Family family = new Family();
            family.setCode(code);
            familyRepository.save(family);

            member.setFamily(family);
            memberRepository.save(member);
            return code;
        }

        // 기존 가족이 있으면 코드만 갱신
        Family family = member.getFamily();
        family.setCode(code);
        familyRepository.save(family);

        return code;
    }


    @Transactional
    public void joinFamily(String inputCode) {
        Member member = authenticatedProvider.getCurrentMember();

        // 입력된 코드에 해당하는 가족이 있는지 조회
        Family familyByInputCode = familyRepository.findByCode(inputCode)
                .orElseThrow(() -> GeneralException.of(StatusCode.UNREGISTERED_FAMILY));

        // 현재 사용자의 가족이 있는 경우
        Family currentFamily = member.getFamily();

        // 사용자의 현재 가족이 입력된 코드와 일치하지 않으면 → 유효하지 않은 코드
        if (currentFamily != null && !currentFamily.getCode().equals(inputCode)) {
            throw GeneralException.of(StatusCode.UNREGISTERED_FAMILY);  // or 새로운 StatusCode.INVALID_FAMILY_CODE
        }

        // 정상적인 경우 → 가족 연결
        member.setFamily(familyByInputCode);
        memberRepository.save(member);
    }


    @Transactional
    public String createNewFamilyCode() {
        Member currentUser = authenticatedProvider.getCurrentMember(); // 현재 로그인 사용자

        if (!currentUser.isPregnant()) {
            throw new BusinessException(StatusCode.ONLY_PREGNANT);
        }

        Family family = currentUser.getFamily();
        String newCode = UUID.randomUUID().toString().substring(0, 8); // 8자리만 추출

        family.setCode(newCode);
        familyRepository.save(family);

        return newCode;
    }


    @Transactional(readOnly = true)
    public String getFamilyCode() {
        Member member = authenticatedProvider.getCurrentMember();

        if (member.getFamily() == null) {
            throw BusinessException.of(StatusCode.UNREGISTERED_FAMILY);
        }

        return member.getFamily().getCode();
    }

}
