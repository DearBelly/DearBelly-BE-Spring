package com.hanium.mom4u.domain.question.repository;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import com.hanium.mom4u.domain.family.entity.QDailyQuestion;
import com.hanium.mom4u.domain.family.entity.QQuestion;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyQuestionRepositoryImpl implements DailyQuestionRepositoryCustom {

    private final JPAQueryFactory qf;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

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
        LocalDateTime sKst   = date.atStartOfDay(KST).toLocalDateTime();
        LocalDateTime nextKst = sKst.plusDays(1); // [sKst, nextKst)

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
        LocalDateTime sKst   = date.atStartOfDay(KST).toLocalDateTime();
        LocalDateTime nextKst = sKst.plusDays(1); // [sKst, nextKst)

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
        LocalDateTime sKst   = date.atStartOfDay(KST).toLocalDateTime();
        LocalDateTime nextKst = sKst.plusDays(1); // [sKst, nextKst)

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

        List<Tuple> rows = qf.select(q.id, q.content)
                .from(q)
                .where(excludeContent == null ? null : q.content.ne(excludeContent))
                .orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc())
                .limit(1)
                .fetch();

        return rows.stream().findFirst().map(t -> new QuestionPick() {
            @Override public Long getId() { return t.get(q.id); }
            @Override public String getContent() { return t.get(q.content); }
        });
    }

    @Override
    public Optional<QuestionPick> pickAny() {
        QQuestion q = QQuestion.question;

        Tuple t = qf.select(q.id, q.content)
                .from(q)
                .orderBy(Expressions.numberTemplate(Double.class, "RAND()").asc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(t).map(tt -> new QuestionPick() {
            @Override public Long getId() { return tt.get(q.id); }
            @Override public String getContent() { return tt.get(q.content); }
        });
    }
}
