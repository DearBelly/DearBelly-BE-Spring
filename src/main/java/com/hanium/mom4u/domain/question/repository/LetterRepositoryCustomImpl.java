package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.member.entity.QMember;
import com.hanium.mom4u.domain.question.dto.response.LetterCheckResponseDto;
import com.hanium.mom4u.domain.question.entity.Letter;
import com.hanium.mom4u.domain.question.entity.QLetter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LetterRepositoryCustomImpl implements LetterRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QLetter letter = QLetter.letter;

    /*
    오늘 일자를 기준으로 안 읽은 편지가 있는지 조회
     */
    public Optional<LetterCheckResponseDto> findOneByReceiverId(Long receiverId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay(); // LocalDate -> LocalDateTime
        LocalDateTime end = date.plusDays(1).atStartOfDay(); // 다음 날의 시작 시각

        BooleanBuilder builder = new BooleanBuilder()
                .and(letter.receiver.id.eq(receiverId))
                .and(letter.createdAt.goe(start)) // 이상
                .and(letter.createdAt.lt(end));  // 이하

        // 오늘날짜에 대하여 없을 수도 있음
        Optional<Letter> foundLetter = Optional.ofNullable(jpaQueryFactory.selectFrom(letter)
                                            .where(builder)
                                            .fetchFirst());

        QMember member = QMember.member;

        // familyId 조회
        Long familyId = jpaQueryFactory.select(member.family.id)
                .from(member)
                .where(member.id.eq(receiverId))
                .fetchOne();

        LetterCheckResponseDto dto = LetterCheckResponseDto.builder()
                .memberId(receiverId)
                .familyId(familyId)
                .isRead(false) // 읽지 않은 것이 존재함을 나타냄
                .build();

        // 만약에 전부 읽었다면 읽었다고 반환
        return Optional.ofNullable(dto);
    }
}
