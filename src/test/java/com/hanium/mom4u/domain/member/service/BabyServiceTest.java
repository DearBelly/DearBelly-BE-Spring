package com.hanium.mom4u.domain.member.service;

import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import com.hanium.mom4u.domain.member.dto.response.BabyInfoResponseDto;
import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@ExtendWith(MockitoExtension.class)
class BabyServiceTest {

    @Mock
    private BabyRepository babyRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AuthenticatedProvider authenticatedProvider;
    @InjectMocks
    private BabyService babyService;

    private Member testMember1;
    private Member testMember2;
    private Member testMember3;
    private Family testFamily;
    private Baby testBaby;

    @BeforeEach
    void setUp() {
        testFamily = new Family();
        ReflectionTestUtils.setField(testFamily, "id", 1L);

        testMember1 = new Member(1L, "test1@naver.com", "test1", "test1", SocialType.NAVER, "1", Role.ROLE_USER, Gender.FEMALE, LocalDate.of(2025, 10, 10), true, LocalDate.of(2025, 10, 10), false);
        testMember2 = new Member(2L, "test2@naver.com", "test2", "test2", SocialType.NAVER, "2", Role.ROLE_USER, Gender.FEMALE, LocalDate.of(2025, 10, 10), true, LocalDate.of(2025, 10, 10), false);
        testMember3 = new Member(3L, "test3@naver.com", "test3", "test3", SocialType.NAVER, "3", Role.ROLE_USER, Gender.MALE, null, true, null, false);
        testMember1.setFamily(testFamily);
        testMember3.setFamily(testFamily);

        testBaby = new Baby(1L, false, Gender.MALE, "testBaby", 2025, testMember1);
    }

    @Nested
    @DisplayName("태아 정보 등록 테스트")
    class SaveBabyTest {

        @Test
        @DisplayName("본인 태아 정보 등록 성공")
        void 본인_태아정보_등록_성공() {
            // given
            BabyInfoRequestDto requestDto = new BabyInfoRequestDto(
                    1L, "newBaby", Gender.UNKNOWN);

            when(authenticatedProvider.getCurrentMember()).thenReturn(testMember1);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));

            // 실제 Baby 객체가 어떻게 생성되는지 확인하기 위한 ArgumentCaptor 사용
            ArgumentCaptor<Baby> babyCaptor = ArgumentCaptor.forClass(Baby.class);

            when(babyRepository.save(babyCaptor.capture())).thenAnswer(invocation -> {
                Baby baby = invocation.getArgument(0);
                ReflectionTestUtils.setField(baby, "id", 100L);
                return baby;
            });

            // when
            BabyInfoResponseDto result = babyService.saveBaby(requestDto);

            // then
            // 1. 결과 검증
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("newBaby");
            assertThat(result.getBabyGender()).isEqualTo(Gender.UNKNOWN);

            // 2. 저장된 Baby 객체 검증
            Baby savedBaby = babyCaptor.getValue();
            assertThat(savedBaby.getName()).isEqualTo("newBaby");
            assertThat(savedBaby.getBabyGender()).isEqualTo(Gender.UNKNOWN);
            assertThat(savedBaby.getMember()).isEqualTo(testMember1);
            assertThat(savedBaby.getMember().getId()).isEqualTo(1L);

            // 3. Mock 호출 검증
            verify(authenticatedProvider, times(2)).getCurrentMember();
            verify(memberRepository, times(1)).findById(1L);
            verify(babyRepository, times(1)).save(any(Baby.class));
        }

        @Test
        @DisplayName("타인이 태아정보 등록 시도 실패")
        void 타인이_태아정보_등록_시도_실패() {
            // given
            BabyInfoRequestDto requestDto = new BabyInfoRequestDto(
                    2L, "newBaby", Gender.MALE);

            when(authenticatedProvider.getCurrentMember()).thenReturn(testMember1);

            // when & then
            assertThatThrownBy(() -> babyService.saveBaby(requestDto))
                    .isInstanceOf(GeneralException.class)
                    .hasMessageContaining("회원을 조회할 수 없습니다.");

            verify(babyRepository, never()).save(any(Baby.class));
        }

        @Test
        @DisplayName("존재하지 않는 memberId로 태아 정보 등록 시도 - 실패")
        void 존재하지않는_memberId로_등록_실패() {
            // given
            BabyInfoRequestDto requestDto = new BabyInfoRequestDto(
                    999L, "newBaby", Gender.MALE);

            when(authenticatedProvider.getCurrentMember()).thenReturn(testMember1);

            // when & then
            assertThatThrownBy(() -> babyService.saveBaby(requestDto))
                    .isInstanceOf(GeneralException.class);

            verify(babyRepository, never()).save(any(Baby.class));
        }

        @Test
        @DisplayName("배우자 태아 정보 등록 성공")
        void 배우자_태아정보_등록_성공() {
            // given
            BabyInfoRequestDto requestDto = new BabyInfoRequestDto(
                    3L, "newBaby", Gender.FEMALE);

            when(authenticatedProvider.getCurrentMember()).thenReturn(testMember3);
            when(memberRepository.findById(3L)).thenReturn(Optional.of(testMember3));

            ArgumentCaptor<Baby> babyCaptor = ArgumentCaptor.forClass(Baby.class);
            when(babyRepository.save(babyCaptor.capture())).thenAnswer(invocation -> {
                Baby baby = invocation.getArgument(0);
                ReflectionTestUtils.setField(baby, "id", 100L);
                return baby;
            });

            // when
            BabyInfoResponseDto result = babyService.saveBaby(requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("newBaby");

            Baby savedBaby = babyCaptor.getValue();
            assertThat(savedBaby.getMember().getId()).isEqualTo(3L);

            verify(babyRepository, times(1)).save(any(Baby.class));
        }
    }
}