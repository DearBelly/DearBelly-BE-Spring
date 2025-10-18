package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.question.entity.DailyQuestion;
import com.hanium.mom4u.domain.question.entity.Question;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepositoryCustom;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final DailyQuestionRepository dailyQuestionRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int LOCK_TIMEOUT_SECONDS = 10;

    @PersistenceContext
    private EntityManager em;

    /*
    '오늘 질문' 생성 w/ LOCK
     */
    public void ensureTodayGlobalQuestion() {
        long t = System.nanoTime();
        LocalDate today = LocalDate.now(KST);
        String lockName = null;
        boolean locked = false;

        try {
            // 이미 '오늘 질문' 있는지 확인
            if (dailyQuestionRepository.existsGlobalOn(today)) {
                log.info("[QUESTION] 이미 존재합니다( date = {} )", today);
                return;
            }

            // 어제자랑 안겹치게 하기 위하여 (어제 기록 없으면 null)
            String yesterdayText = dailyQuestionRepository.findOneGlobalOn(today.minusDays(1))
                            .map(DailyQuestion::getDailyQuestionText)
                                    .orElse(null);

            // 어제랑 안 겹치는 랜덤 pick
            var pick = dailyQuestionRepository.pickRandomExcluding(yesterdayText)
                    .or(() -> dailyQuestionRepository.pickAny())
                    .orElseThrow(() -> GeneralException.of(StatusCode.QUESTION_NOT_FOUND));

            // Lock
            lockName = generateLockName(today);
            locked = getLock(lockName);

            if (!locked) {
                log.warn("[LOCK] failed to acquire lock: {}", lockName);
                throw GeneralException.of(StatusCode.FAILED_TO_GET_LOCK);
            }

            // Transaction -> 데이터 삽입(오늘 질문 생성)
            try {
                createDailyQuestion(today, pick);
            } catch (Exception e) {
                // Transaction 실패 분류
                log.error("[QUESTION] failed during transaction", e);
                throw GeneralException.of(StatusCode.FAILED_CREATE_DAILY_Q);
            }

        } catch (Exception e) {
            log.error("[QUESTION] FAILED", e);
            throw GeneralException.of(StatusCode.QUESTION_SCHEDULER_ERROR);
        } finally {
            // 데드락 방지
            if (locked && lockName != null) {
                try {
                    releaseLock(lockName);
                } catch (Exception e) {
                    log.error("[LOCK] failed to release lock {}, DeadLock", lockName, e);
                }
            }
            long tookMs = (System.nanoTime() - t)/1_000_000;
            log.info("[QUESTION] END took = {} ms", tookMs);
        }
    }

    /*
    GET_LOCK 실행 후 성공 여부에 대하여 반환하는 메서드
     */
    private boolean getLock(String lockName) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT GET_LOCK(:lockName, :timeout)")
                    .setParameter("lockName", lockName)
                    .setParameter("timeout", LOCK_TIMEOUT_SECONDS)
                    .getSingleResult();

            // Lock 획득(완료여부)
            boolean acquired = (result != null && ((Number)result).intValue() == 1);

            if (acquired) {
                log.info("[LOCK] acquired lock={}", lockName);
            } else if (result != null && ((Number)result).intValue() == 0) {
                log.warn("[LOCK] lock timeout = {}", lockName);
            } else {
                log.error("[LOCK] lock not acquired={} (NULL of -1)", lockName);
            }
            return acquired;
        } catch (Exception e) {
            log.error("[LOCK] Exception during GET_LOCK: {}", lockName, e);
            return false;
        }
    }

    /*
    Lock 고유의 이름을 생성하기 위한 메서드 : "global_question_" + 오늘날짜로 설정
     */
    private String generateLockName(LocalDate today) {
        return "global_question_" + today.toString();
    }

    private void releaseLock(String lockName) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT RELEASE_LOCK(:lockName)")
                    .setParameter("lockName", lockName)
                    .getSingleResult();

            if (result != null && ((Number)result).intValue() == 1) {
                log.info("[LOCK] lock successfully released={}", lockName);
            } else {
                log.warn("[LOCK] lock not released={}", lockName);
            }
        } catch (Exception e) {
            log.error("[LOCK] Exception during RELEASE_LOCK: {}", lockName, e);
        }
    }

    /*
    실제 오늘 질문을 생성하는 메서드
    오늘 날짜와 Projection Interface 사용
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5)
    public void createDailyQuestion(LocalDate today, DailyQuestionRepositoryCustom.QuestionPick pick) {

        // 락 획득 후 다시 확인 (더블 체크)
        if (dailyQuestionRepository.existsGlobalOn(today)) {
            log.info("[QUESTION] already created for date={}", today);
            return;
        }

        Question qRef = em.getReference(Question.class, pick.getId());

        DailyQuestion created = new DailyQuestion();
        created.setFamily(null);
        created.setQuestion(qRef);
        created.setOriginQuestion(qRef);
        created.setDailyQuestionText(pick.getContent());

        dailyQuestionRepository.save(created);
        em.close();
    }


    @Transactional(readOnly = true)
    public DailyQuestion getFor(LocalDate date, Long familyIdOrNull) {
        if (familyIdOrNull != null) {
            return dailyQuestionRepository.findOneForDate(familyIdOrNull, date)
                    .or(() -> dailyQuestionRepository.findOneGlobalOn(date))
                    .orElse(null);
        }
        return dailyQuestionRepository.findOneGlobalOn(date).orElse(null);
    }

    @Transactional(readOnly = true)
    public String getTextFor(LocalDate date, Long familyIdOrNull) {
        DailyQuestion dq = getFor(date, familyIdOrNull);
        return (dq == null) ? null : dq.getQuestionText();
    }
}
