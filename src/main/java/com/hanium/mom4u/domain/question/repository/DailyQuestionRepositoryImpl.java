package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.question.entity.DailyQuestion;
import com.hanium.mom4u.domain.question.entity.QDailyQuestion;
import com.hanium.mom4u.domain.question.entity.QQuestion;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyQuestionRepositoryImpl implements DailyQuestionRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private LocalDateTime startOfDayKst(LocalDate date) {
        return date.atStartOfDay(KST).toLocalDateTime();
    }

    private LocalDateTime nextDayStartKst(LocalDate date) {
        return startOfDayKst(date).plusDays(1); // [start, next)
    }
    @Override
    public Optional<DailyQuestion> findOneForDate(Long familyId, LocalDate date) {
        if (familyId != null) {
            var fam = findByDateAndFamily(date, familyId);
            if (fam.isPresent()) return fam;
        }
        return findOneGlobalOn(date);
    }

    private Optional<DailyQuestion> findByDateAndFamily(LocalDate date, Long familyId) {
        QDailyQuestion dq = QDailyQuestion.dailyQuestion;
        LocalDateTime sKst   = startOfDayKst(date);
        LocalDateTime nextKst = nextDayStartKst(date);

        DailyQuestion found = qf.selectFrom(dq)
                .where(dq.family.id.eq(familyId)
                        .and(dq.createdAt.goe(sKst))
                        .and(dq.createdAt.lt(nextKst)))
                .orderBy(dq.createdAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(found);
    }


    @Override
    public Optional<DailyQuestion> findOneGlobalOn(LocalDate date) {
        QDailyQuestion dq = QDailyQuestion.dailyQuestion;
        LocalDateTime sKst   = startOfDayKst(date);
        LocalDateTime nextKst = nextDayStartKst(date);

        DailyQuestion found = qf.selectFrom(dq)
                .where(dq.family.isNull()
                        .and(dq.createdAt.goe(sKst))
                        .and(dq.createdAt.lt(nextKst)))
                .orderBy(dq.createdAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(found);
    }


    @Override
    public boolean existsGlobalOn(LocalDate date) {
        QDailyQuestion dq = QDailyQuestion.dailyQuestion;
        LocalDateTime sKst   = startOfDayKst(date);
        LocalDateTime nextKst = nextDayStartKst(date);

        Integer one = qf.selectOne()
                .from(dq)
                .where(dq.family.isNull()
                        .and(dq.createdAt.goe(sKst))
                        .and(dq.createdAt.lt(nextKst)))
                .fetchFirst();

        return one != null;
    }

    @Override
    public Optional<QuestionPick> pickRandomExcluding(String excludeContent) {
        QQuestion q = QQuestion.question;

        // min/max 한 번에 조회 ( 인덱스 사용)
        var minExpr = q.id.min();
        var maxExpr = q.id.max();
        Tuple range = qf.select(minExpr, maxExpr).from(q).fetchOne();
        if (range == null) return Optional.empty();

        Long minId = range.get(minExpr);
        Long maxId = range.get(maxExpr);
        if (minId == null || maxId == null) return Optional.empty();

        // 1) 랜덤 시작점 ≥ rand 에서 1건 (id 인덱스 사용)
        long rand = java.util.concurrent.ThreadLocalRandom.current().nextLong(minId, maxId + 1);

        Tuple first = qf.select(q.id, q.content)
                .from(q)
                .where(
                        q.id.goe(rand),
                        (excludeContent == null ? null : q.content.ne(excludeContent))
                )
                .orderBy(q.id.asc())
                .limit(1)
                .fetchOne();

        // 2) 없으면 wrap-around: 테이블 앞쪽에서 1건
        Tuple picked = (first != null) ? first
                : qf.select(q.id, q.content)
                .from(q)
                .where(excludeContent == null ? null : q.content.ne(excludeContent))
                .orderBy(q.id.asc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(picked).map(t -> new QuestionPick() {
            @Override public Long getId() { return t.get(q.id); }
            @Override public String getContent() { return t.get(q.content); }
        });
    }

    @Override
    public Optional<QuestionPick> pickAny() {
        // exclude 없이 동일 로직 재사용
        return pickRandomExcluding(null);
    }
}