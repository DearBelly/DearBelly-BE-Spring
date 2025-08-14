package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.question.dto.request.LetterRequest;
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
    private final LetterReadService letterreadService;

    public Long create(LetterRequest req) {
        Member me = authenticatedProvider.getCurrentMember(); // 가족 정보 확인용
        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        // 가족이 없어도 저장 가능 (family=null)
        Letter letter = Letter.builder()
                .content(content)
                .writer(me)
                .family(me.getFamily()) // null 가능
                .build();

        return letterRepository.save(letter).getId();
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
    }

    public void delete(Long letterId) {
        Long myId = authenticatedProvider.getCurrentMemberId();
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));
        if (!letter.getWriter().getId().equals(myId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        letterRepository.delete(letter);
    }

    @Transactional(readOnly = true)
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

        //  읽음 처리는 별도 트랜잭션으로
        if (letter.getFamily() != null && !letter.getWriter().getId().equals(me.getId())) {
            letterreadService.markAsRead(letter, me);
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


}

