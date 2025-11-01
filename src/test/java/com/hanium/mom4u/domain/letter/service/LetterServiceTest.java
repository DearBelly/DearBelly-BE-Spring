package com.hanium.mom4u.domain.letter.service;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.letter.dto.request.LetterRequest;
import com.hanium.mom4u.domain.letter.dto.response.LetterCheckResponseDto;
import com.hanium.mom4u.domain.letter.dto.response.LetterResponse;
import com.hanium.mom4u.domain.letter.entity.Letter;
import com.hanium.mom4u.domain.letter.entity.LetterRead;
import com.hanium.mom4u.domain.letter.repository.LetterReadRepository;
import com.hanium.mom4u.domain.letter.repository.LetterRepository;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
import com.hanium.mom4u.domain.question.service.QuestionService;
import com.hanium.mom4u.domain.sse.dto.MessageDto;
import com.hanium.mom4u.external.redis.publisher.MessagePublisher;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Transactional
class LetterServiceTest {

    @InjectMocks
    private LetterService letterService;

    @Mock
    private LetterRepository letterRepository;
    @Mock
    private LetterReadRepository letterReadRepository;
    @Mock private AuthenticatedProvider authenticatedProvider;
    @Mock private BabyRepository babyRepository;
    @Mock private MemberRepository memberRepository;
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

    private static Member member(Long id, String nickname, String providerId) {
        Member m = newInstance(Member.class);
        setDeep(m, "id", id);
        setDeep(m, "nickname", nickname);
        setDeep(m, "providerId", providerId);
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

    private Family testFamily;
    private Member testMember;
    private Letter testLetter;

    @BeforeEach
    void mockToday() {
        localDateMock = Mockito.mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        LocalDate fixed = LocalDate.of(2025, 9, 21);

        // 서비스에서 사용할 정확한 오버로드만 스텁 (any() 쓰지 말 것!)
        localDateMock.when(LocalDate::now).thenReturn(fixed);
        localDateMock.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixed);

        testMember = member(1L, "testMember", "testMember");

        testFamily = famWithMembers(testMember);
    }

    @AfterEach
    void tearDown() {
        if (localDateMock != null) localDateMock.close();
    }

    // ===== 테스트 =====

    @Test
    @DisplayName("create: 가족 편지 작성 성공 시 - 나 제외 가족에게 알림, 읽음 플래그 갱신")
    void create_success_familyPublishesAndFlags() {
        Member me   = member(1L, "me", "me");
        List<Member> memberList = new LinkedList<>();
        Member you  = member(2L, "you", "you");
        Member you2 = member(3L, "you2", "you2");
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

        // 알람 발송
        ArgumentCaptor<MessageDto> msgCap = ArgumentCaptor.forClass(MessageDto.class);
        verify(messagePublisher, times(2)).publish(eq("Alarm"), msgCap.capture());
        assertThat(msgCap.getAllValues())
                .extracting(MessageDto::getReceiverId)
                .containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    @DisplayName("create: 같은 날 두 번 쓰면 예외(LETTER_TODAY_ALREADY_WRITTEN)")
    void create_duplicateToday_throws() {
        // given
        testLetter = savedLetter(1L,
                testMember,
                testFamily,
                "오늘의 편지",
                LocalDateTime.now());

        LetterRequest letterRequest = req("오늘의 편지");

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(memberRepository.findWithFamilyAndMembers(1L))
                .thenReturn(Optional.of(testMember));
        when(letterRepository.findTodayByWriterId(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(testLetter));

        // when & then
        assertThatThrownBy(() -> letterService.create(letterRequest))
                .isInstanceOf(GeneralException.class)
                .hasFieldOrPropertyWithValue("statusCode", StatusCode.LETTER_TODAY_ALREADY_WRITTEN);
    }

    @Test
    @DisplayName("가족이 없는 경우 본인에게만 편지를 저장")
    void 가족이없는경우_본인_편지_저장() {
        // given
        Member memberWithoutFamily = member(2L, "testMember2", "testMember2");

        LetterRequest request = new LetterRequest();
        ReflectionTestUtils.setField(request, "content", "혼자 쓰는 편지");

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(2L);
        when(memberRepository.findWithFamilyAndMembers(2L))
                .thenReturn(Optional.of(memberWithoutFamily));
        when(letterRepository.findTodayByWriterId(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // when
        letterService.create(request);

        // then
        ArgumentCaptor<List<LetterRead>> captor = ArgumentCaptor.forClass(List.class);
        verify(letterReadRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        verify(messagePublisher, never()).publish(anyString(), any());
    }


    @Test
    @DisplayName("getDetail: 가족의 '남이 쓴' 편지 조회")
    void getDetail_marksSeenForOtherFamilyLetter() {
        // given
        Member me = member(2L, "me", "me");
        Member other = member(3L, "other", "other");
        Family fam = famWithMembers(me, other);

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(2L);
        when(memberRepository.findWithFamilyAndMembers(2L)).thenReturn(Optional.of(me));

        Letter letter = savedLetter(200L, other, fam, "안녕", LocalDateTime.of(2025, 9, 21, 10, 0));
        when(letterRepository.findById(200L)).thenReturn(Optional.of(letter));
        when(questionService.getTextFor(any(LocalDate.class), any()))
                .thenReturn("오늘의 질문");

        // when
        LetterResponse res = letterService.getDetail(200L);

        // then
        assertThat(res.getId()).isEqualTo(200L);
        assertThat(res.isEditable()).isFalse();
        assertThat(res.isRead()).isTrue(); // 읽음 처리 확인
        verify(letterReadRepository).markRead(eq(200L), eq(2L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("본인이 작성한 편지 삭제 성공")
    void delete_MyLetter_Success() {
        // given
        testLetter = savedLetter(1L,
                testMember,
                testFamily,
                "오늘의 편지",
                LocalDateTime.now());

        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(letterRepository.findById(testLetter.getId())).thenReturn(Optional.of(testLetter));

        // when
        letterService.delete(testLetter.getId());

        // then
        verify(letterRepository, times(1)).delete(testLetter);
    }

    @Test
    @DisplayName("오늘 읽지 않은 편지가 확인 성공")
    void getLetterCheck_HasUnread_ReturnsTrue() {
        // given
        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(letterRepository.findExistsByMemberId(eq(1L), any(LocalDate.class)))
                .thenReturn(true);

        // when
        LetterCheckResponseDto result = letterService.getLetterCheck();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.isUnreadExist()).isTrue();
    }

    @Test
    @DisplayName("읽지 않은 편지가 없으면 false를 반환 성공")
    void getLetterCheck_NoUnread_ReturnsFalse() {
        // given
        when(authenticatedProvider.getCurrentMemberId()).thenReturn(1L);
        when(letterRepository.findExistsByMemberId(eq(1L), any(LocalDate.class)))
                .thenReturn(false);

        // when
        LetterCheckResponseDto result = letterService.getLetterCheck();

        // then
        assertThat(result).isNotNull();
        assertThat(result.isUnreadExist()).isFalse();
    }
}