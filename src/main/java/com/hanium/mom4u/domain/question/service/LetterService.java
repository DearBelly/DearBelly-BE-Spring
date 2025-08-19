package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.question.dto.request.LetterRequest;
import com.hanium.mom4u.domain.question.dto.response.HomeResponse;
import com.hanium.mom4u.domain.question.dto.response.LetterResponse;
import com.hanium.mom4u.domain.question.entity.Letter;
import com.hanium.mom4u.domain.question.repository.LetterRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LetterService {

    private final LetterRepository letterRepository;
    private final AuthenticatedProvider authenticatedProvider;
    private final BabyRepository babyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(LetterRequest req) {
        Member me = authenticatedProvider.getCurrentMember();
        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        Letter letter = Letter.builder()
                .content(content)
                .writer(me)
                .family(me.getFamily()) // null 가능
                .build();

        Long id = letterRepository.save(letter).getId();

        if (me.getFamily() != null) {
            letterRepository.resetSeenFlagForFamilyExceptWriter(me.getFamily().getId(), me.getId());
            letterRepository.markSeenForMember(me.getId());
        }
        return id;
    }


    @Transactional(readOnly = true)
    public List<LetterResponse> getByMonth(Integer year, Integer month) {
        Member me = authenticatedProvider.getCurrentMember();
        YearMonth ym = (year == null || month == null) ? YearMonth.now() : YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23,59,59);

        List<Letter> letters;
        if (me.getFamily() != null) {
            // 가족이 있으면 가족 편지 전체
            letters = letterRepository.findByFamilyAndCreatedAtBetween(me.getFamily(), start, end);
        } else {
            // 가족이 없으면 내 편지만
            letters = letterRepository.findByWriterAndCreatedAtBetween(me, start, end);
        }

        return letters.stream()
                .map(l -> LetterResponse.builder()
                        .id(l.getId())
                        .content(l.getContent())
                        .nickname(l.getWriter().getNickname())
                        .imgUrl(l.getWriter().getImgUrl())
                        .createdAt(l.getCreatedAt())
                        .editable(l.getWriter().getId().equals(me.getId()))
                        .build())
                .toList();
    }

    @Transactional
    public void update(Long letterId, LetterRequest req) {
        Long myId = authenticatedProvider.getCurrentMemberId();
        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));
        if (!letter.getWriter().getId().equals(myId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        letter.updateContent(content);

        if (letter.getFamily() != null) {
            letterRepository.resetSeenFlagForFamilyExceptWriter(letter.getFamily().getId(), myId);
            letterRepository.markSeenForMember(myId); // 선택
        }
    }
    public void delete(Long letterId) {
        Long myId = authenticatedProvider.getCurrentMemberId();
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));
        if (!letter.getWriter().getId().equals(myId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        letterRepository.delete(letter);
    }

    @Transactional
    public LetterResponse getDetail(Long letterId) {
        Member me = authenticatedProvider.getCurrentMember();
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));

        // 권한 체크
        if (letter.getFamily() != null) {
            if (me.getFamily() == null || !letter.getFamily().getId().equals(me.getFamily().getId())) {
                throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);
            }
        } else if (!letter.getWriter().getId().equals(me.getId())) {
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);
        }

        // 남이 쓴 가족 편지라면 읽음 처리
        boolean shouldMarkSeen =
                letter.getFamily() != null
                        && me.getFamily() != null
                        && letter.getFamily().getId().equals(me.getFamily().getId())
                        && !letter.getWriter().getId().equals(me.getId());

        if (shouldMarkSeen) {
            letterRepository.markSeenForMember(me.getId());
        }

        return LetterResponse.builder()
                .id(letter.getId())
                .content(letter.getContent())
                .nickname(letter.getWriter().getNickname())
                .imgUrl(letter.getWriter().getImgUrl())
                .createdAt(letter.getCreatedAt())
                .editable(letter.getWriter().getId().equals(me.getId()))
                .build();
    }


    /** 아기 이름, 주차(0부터), 편지 읽음 여부 */
    @Transactional(readOnly = true)
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
                    .babyName(null)
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
        if (memberRepository.countByFamilyId(me.getFamily().getId()) <= 1) return false;

        return !me.isHasSeenFamilyLetters();
    }
}

