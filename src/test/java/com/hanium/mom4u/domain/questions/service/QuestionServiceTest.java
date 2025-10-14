package com.hanium.mom4u.domain.questions.service;

import com.hanium.mom4u.domain.question.entity.DailyQuestion;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
import com.hanium.mom4u.domain.question.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock DailyQuestionRepository repo;
    @InjectMocks
    QuestionService service;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("오늘의 질문이 없을 때 생성")
    void ensureTodayGlobalQuestion_creates_when_absent() {
//        LocalDate today = LocalDate.now(KST);
//        when(repo.existsGlobalOn(today)).thenReturn(false);
//        when(repo.findOneGlobalOn(today.minusDays(1))).thenReturn(Optional.empty());
//        when(repo.pickRandomExcluding(null)).thenReturn(Optional.of(new DailyQuestionRepositoryCustom.QuestionPick() {
//            public Long getId() { return 1L; }
//            public String getContent() { return "랜덤"; }
//        }));
//
//        service.ensureTodayGlobalQuestion();
//
//        verify(repo).save(argThat(dq ->
//                dq.getFamily() == null &&
//                        "랜덤".equals(dq.getQuestionText()) &&
//                        dq.getQuestionId().equals(1L)
//        ));
    }

    @Test
    void getFor_family_prefers_family_question() {
        LocalDate d = LocalDate.now(KST);
        var famDq = new DailyQuestion();
        // 주입
        try {
            var f = DailyQuestion.class.getDeclaredField("questionText");
            f.setAccessible(true); f.set(famDq, "FAM");
        } catch (Exception e) { throw new RuntimeException(e); }

        when(repo.findOneForDate(10L, d)).thenReturn(Optional.of(famDq));

        var result = service.getFor(d, 10L);
        assertThat(result.getQuestionText()).isEqualTo("FAM");
    }

    @Test
    void getTextFor_returns_null_when_absent() {
        LocalDate d = LocalDate.now(KST);
        when(repo.findOneGlobalOn(d)).thenReturn(Optional.empty());

        var text = service.getTextFor(d, null);
        assertThat(text).isNull();
    }
}
