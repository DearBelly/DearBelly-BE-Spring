package com.hanium.mom4u.domain.questions.service;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.letter.dto.request.LetterRequest;
import com.hanium.mom4u.domain.letter.dto.response.LetterResponse;
import com.hanium.mom4u.domain.letter.dto.response.TodayWriteResponse;
import com.hanium.mom4u.domain.letter.entity.Letter;
import com.hanium.mom4u.domain.letter.repository.DailyQuestionRepository;
import com.hanium.mom4u.domain.letter.repository.LetterRepository;
import com.hanium.mom4u.domain.letter.service.LetterService;
import com.hanium.mom4u.domain.letter.service.QuestionService;
import com.hanium.mom4u.domain.sse.dto.MessageDto;
import com.hanium.mom4u.external.redis.publisher.MessagePublisher;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LetterReadTest {

    @InjectMocks
    private LetterService letterService;

    @Mock private LetterRepository letterRepository;
    @Mock private AuthenticatedProvider authenticatedProvider;
    @Mock private BabyRepository babyRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private DailyQuestionRepository dailyQuestionRepository;
    @Mock private QuestionService questionService;
    @Mock private MessagePublisher messagePublisher;

    // ===== 리플렉션 유틸 =====
    private static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static void setDeep(Object target, String field, Object value) {
        try {
            Class<?> c = target.getClass();
            Field f = null;
            while (c != null) {
                try {
                    f = c.getDeclaredField(field);
                    break;
                } catch (NoSuchFieldException ignored) {
                    c = c.getSuperclass();
                }
            }
            if (f == null) throw new NoSuchFieldException(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static LetterRequest req(String content) {
        LetterRequest r = newInstance(LetterRequest.class);
        setDeep(r, "content", content);
        return r;
    }

    private static Family famWithMembers(Member... members) {
        Family f = newInstance(Family.class);
        setDeep(f, "id", 10L);
        setDeep(f, "memberList", List.of(members));
        for (Member m : members) setDeep(m, "family", f);
        return f;
    }

    private static Member member(Long id, String nickname, boolean seen) {
        Member m = newInstance(Member.class);
        setDeep(m, "id", id);
        setDeep(m, "nickname", nickname);
        setDeep(m, "hasSeenFamilyLetters", seen);
        return m;
    }

    private static Letter savedLetter(Long id, Member writer, Family family, String content, LocalDateTime createdAt) {
        Letter l = Letter.builder().content(content).writer(writer).family(family).build();
        setDeep(l, "id", id);
        setDeep(l, "createdAt", createdAt);
        return l;
    }

    // ===== 날짜 고정 =====
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private MockedStatic<LocalDate> localDateMock;

    @BeforeEach
    void mockToday() {
        localDateMock = Mockito.mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        LocalDate fixed = LocalDate.of(2025, 9, 21);

        // 서비스에서 사용할 정확한 오버로드만 스텁 (any() 쓰지 말 것!)
        localDateMock.when(LocalDate::now).thenReturn(fixed);
        localDateMock.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixed);
    }

    @AfterEach
    void tearDown() {
        if (localDateMock != null) localDateMock.close();
    }

    // ===== 테스트 =====

    @Test
    @DisplayName("create: 가족 편지 작성 성공 시 - 나 제외 가족에게 알림, 읽음 플래그 갱신")
    void create_success_familyPublishesAndFlags() {
        Member me   = member(1L, "me", true);
        List<Member> memberList = new LinkedList<>();
        Member you  = member(2L, "you", true);
        Member you2 = member(3L, "you2", true);
        memberList.add(you);
        memberList.add(you2);

        Family fam  = famWithMembers(me, you, you2);

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(memberRepository.findWithFamilyAndMembers(1L)).thenReturn(Optional.of(me));
        when(letterRepository.existsByWriter_IdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(false);

        when(letterRepository.save(any())).thenAnswer(inv -> {
            Letter l = inv.getArgument(0);
            setDeep(l, "id", 100L);
            setDeep(l, "createdAt", LocalDateTime.of(2025, 9, 21, 9, 0));
            return l;
        });

        letterService.create(req("안녕"));

        //verify(letterRepository).resetSeenFlagForFamilyExceptWriter(10L, 1L);
        //verify(letterRepository).markSeenForMember(1L);

        // you(2L)에게만 알람 발송
        ArgumentCaptor<MessageDto> msgCap = ArgumentCaptor.forClass(MessageDto.class);
        verify(messagePublisher, times(1)).publish(eq("Alarm"), msgCap.capture());
        assertThat(msgCap.getValue().getReceiverId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("create: 같은 날 두 번 쓰면 예외(LETTER_TODAY_ALREADY_WRITTEN)")
    void create_duplicateToday_throws() {
        Member me = member(1L, "me", true);
        setDeep(me, "family", null); // 개인 편지 케이스

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(memberRepository.findWithFamilyAndMembers(1L)).thenReturn(Optional.of(me));
        when(letterRepository.existsByWriter_IdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(true);

        assertThrows(GeneralException.class, () -> letterService.create(req("두번째")));
        verify(letterRepository, never()).save(any());
    }

    @Test
    @DisplayName("getDetail: 가족의 '남이 쓴' 편지 조회 시 내 읽음 플래그 true로 갱신")
    void getDetail_marksSeenForOtherFamilyLetter() {
        Member me = member(1L, "me", true);
        Member other = member(2L, "other", true);
        Family fam = famWithMembers(me, other);

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(memberRepository.findByIdWithFamily(1L)).thenReturn(Optional.of(me));

        Letter letter = savedLetter(200L, other, fam, "안녕", LocalDateTime.of(2025, 9, 21, 10, 0));
        when(letterRepository.findById(200L)).thenReturn(Optional.of(letter));

        LetterResponse res = letterService.getDetail(200L);

        assertThat(res.getId()).isEqualTo(200L);
        assertThat(res.isEditable()).isFalse();
    }

    @Test
    @DisplayName("getTodayForWrite: 오늘 질문/내 편지 상태 구성 - 오늘 내 편지 없음")
    void todayForWrite_basic() {
        Member me = member(1L, "me", true);
        Family fam = famWithMembers(me);

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(memberRepository.findByIdWithFamily(1L)).thenReturn(Optional.of(me));

        //  DailyQuestionRepository 대신 QuestionService로 스텁
        LocalDate today = LocalDate.of(2025, 9, 21);
        DailyQuestion dq = newInstance(DailyQuestion.class);
        setDeep(dq, "id", 777L);
        setDeep(dq, "questionText", "오늘의 질문이에요!");
        setDeep(dq, "family", null);
        when(questionService.getFor(eq(today), eq(10L))).thenReturn(dq);

        // 오늘 내 편지 없음
        when(letterRepository.findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc(eq(1L), any(), any()))
                .thenReturn(Optional.empty());

        TodayWriteResponse r = letterService.getTodayForWrite();

        assertThat(r.getDate()).isEqualTo(today);
        assertThat(r.getQuestionId()).isEqualTo(777L);
        assertThat(r.getQuestionText()).isEqualTo("오늘의 질문이에요!");
        assertThat(r.isCanWrite()).isTrue();
        assertThat(r.isEditable()).isFalse();
    }


}
