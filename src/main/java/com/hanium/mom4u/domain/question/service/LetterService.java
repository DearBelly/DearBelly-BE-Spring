package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.question.common.HomeTheme;
import com.hanium.mom4u.domain.question.dto.request.LetterRequest;
import com.hanium.mom4u.domain.question.dto.response.*;
import com.hanium.mom4u.domain.question.entity.Letter;
import com.hanium.mom4u.domain.question.repository.LetterRepository;
import com.hanium.mom4u.domain.sse.dto.MessageDto;
import com.hanium.mom4u.external.redis.publisher.MessagePublisher;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterService {

    private final LetterRepository letterRepository;
    private final AuthenticatedProvider authenticatedProvider;
    private final BabyRepository babyRepository;
    private final MemberRepository memberRepository;

    //  질문 관련 로직은 전부 QuestionService로 위임
    private final QuestionService questionService;

    private final MessagePublisher messagePublisher;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String THEME_COOKIE = "HOME_THEME";

    @Value("${spring.home.theme.cookie}")
    private int COOKIE_MAX_AGE ;

    @Value("${spring.cloud.aws.s3.default-image}")
    private String DEFAULT_IMAGE;

    private String profileUrlOrDefault(Member m) {
        String url = (m == null) ? null : m.getImgUrl();
        return (url == null || url.isBlank()) ? DEFAULT_IMAGE : url;
        // DEFAULT_IMAGE는 설정값(환경변수/설정파일)에서 주입
    }

    /*
     * 편지 작성하기
     */
    @Transactional
    public void create(LetterRequest req) {

        Long memberId = authenticatedProvider.getCurrentMemberId();
        // 가족 + 구성원까지 fetch-join (편지 수신자 리스트 필요하므로 이 경우만 무거운 조회 허용)
        Member me = memberRepository.findWithFamilyAndMembers(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        LocalDate today = LocalDate.now(KST);

        // 버그 수정: 이미 작성된 편지가 "존재하면" 예외
        if (letterRepository.findOneByReceiverId(memberId, today).isPresent()) {
            log.warn("이미 오늘 편지를 작성했습니다. memberId={}, date={}", memberId, today);
            throw GeneralException.of(StatusCode.LETTER_TODAY_ALREADY_WRITTEN);
        }

        // 수신자: 가족 구성원 전체 (가족 없으면 본인만)
        List<Member> memberList;
        if (me.getFamily() == null || me.getFamily().getMemberList() == null || me.getFamily().getMemberList().isEmpty()) {
            memberList = List.of(me);
        } else {
            memberList = me.getFamily().getMemberList();
        }

        // 가족 구성원 수만큼 편지 저장 (수신자별 개별 행)
        List<Letter> letterList = new ArrayList<>(memberList.size());
        for (Member receiver : memberList) {
            Letter letter = Letter.builder()
                    .content(content)
                    .writer(me)
                    .family(me.getFamily())              // null 가능
                    .receiver(receiver)                   // 수신자별 분리 저장
                    .isRead(Objects.equals(receiver.getId(), memberId)) // 내 편지는 즉시 읽음 처리
                    .build();
            letterList.add(letter);
        }
        letterRepository.saveAll(letterList);

        // 알림: 본인이 아닌 구성원에게만 전송
        if (memberList != null && !memberList.isEmpty()) {
            for (Member m : memberList) {
                if (!Objects.equals(m.getId(), memberId)) {
                    messagePublisher.publish(
                            "Alarm",
                            MessageDto.builder()
                                    .receiverId(m.getId())
                                    .title("새로운 편지가 도착했어요!")
                                    .content("가족이 보낸 편지가 도착했어요. 확인해보세요!")
                                    .build()
                    );
                }
            }
        }
    }

    /*
     * 편지 월별 조회
     */
    @Transactional(readOnly = true)
    public List<LetterResponse> getByMonth(Integer year, Integer month) {
        Long memberId = authenticatedProvider.getCurrentMemberId();
        // 편지 편집 가능 여부(작성자 비교) 때문에 나 자신 정보 + 가족 id만 필요
        Member me = memberRepository.findWithFamilyAndMembers(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        YearMonth ym = (year == null || month == null) ? YearMonth.now(KST) : YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Letter> letters = letterRepository.findLetterByYearAndMonth(memberId, start, end);

        Long famId = (me.getFamily() == null) ? null : me.getFamily().getId();

        return letters.stream().map(l -> {
            String qText = questionService.getTextFor(l.getCreatedAt().toLocalDate(), famId);
            return LetterResponse.builder()
                    .id(l.getId())
                    .content(l.getContent())
                    .nickname(l.getWriter().getNickname())
                    .imgUrl(profileUrlOrDefault(l.getWriter()))
                    .createdAt(l.getCreatedAt())
                    .editable(l.getWriter().getId().equals(me.getId()))
                    .question(qText)
                    .build();
        }).toList();
    }

    /*
     * 편지 수정
     */
    @Transactional
    public void update(Long letterId, LetterRequest req) {
        Long memberId = authenticatedProvider.getCurrentMemberId();
        Member me = memberRepository.findWithFamilyAndMembers(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));

        // 본인이 작성한 편지만 수정 가능
        if (!letter.getWriter().getId().equals(memberId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        List<Member> memberList = (me.getFamily() == null) ? List.of(me) : me.getFamily().getMemberList();
        if (memberList == null || memberList.isEmpty()) memberList = List.of(me);

        // 수신자별 별도 행 구조를 유지한다면, 이 부분은 엔티티 내부에서 receiverId별 업데이트가 필요
        for (Member m : memberList) {
            letter.updateContent(m.getId(), content);
        }
        letterRepository.save(letter);
    }

    @Transactional
    public void delete(Long letterId) {
        Long myId = authenticatedProvider.getCurrentMemberId();
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));
        if (!letter.getWriter().getId().equals(myId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        letterRepository.delete(letter);
    }

    /*
     * 편지 상세 조회
     */
    @Transactional
    public LetterResponse getDetail(Long letterId) {
        Member me = meWithFamily();
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));

        Long myFamId     = (me.getFamily() == null) ? null : me.getFamily().getId();
        Long letterFamId = (letter.getFamily() == null) ? null : letter.getFamily().getId();
        Long writerFamId = (letter.getWriter().getFamily() == null) ? null : letter.getWriter().getFamily().getId();

        boolean isMine             = letter.getWriter().getId().equals(me.getId());
        boolean sameByLetterFamily = (letterFamId != null && myFamId != null && letterFamId.equals(myFamId));
        boolean sameByWriterFamily = (writerFamId != null && myFamId != null && writerFamId.equals(myFamId));

        if (!(isMine || sameByLetterFamily || sameByWriterFamily)) {
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);
        }

        // TODO: 남이 쓴 가족 편지면 읽음 처리 (수신자별 행 구조에 맞춰 구현)
        // if (!isMine && (sameByLetterFamily || sameByWriterFamily)) {
        //     letterRepository.markReadByReceiver(letter.getId(), me.getId());
        // }

        String qText = questionService.getTextFor(letter.getCreatedAt().toLocalDate(), myFamId);

        return LetterResponse.builder()
                .id(letter.getId())
                .content(letter.getContent())
                .nickname(letter.getWriter().getNickname())
                .imgUrl(profileUrlOrDefault(letter.getWriter()))
                .createdAt(letter.getCreatedAt())
                .editable(isMine)
                .question(qText)
                .build();
    }

    @Transactional(readOnly = true)
    public HomeResponse getTopBanner() {
        Member me = meWithFamily();

        // 1) 진행 중인 아기들 전부 가져오기
        List<Baby> babies;
        if (me.getFamily() != null) {
            babies = babyRepository.findOngoingByFamilyId(me.getFamily().getId(), PageRequest.of(0, 100));
            if (babies.isEmpty() && me.isPregnant()) {
                babies = babyRepository.findOngoingByMemberId(me.getId(), PageRequest.of(0, 100));
            }
        } else if (me.isPregnant()) {
            babies = babyRepository.findOngoingByMemberId(me.getId(), PageRequest.of(0, 100));
        } else {
            babies = java.util.Collections.emptyList();
        }

        String babyNames = babies.isEmpty() ? null
                : babies.stream()
                .map(b -> (b.getName() == null || b.getName().isBlank()) ? "아기" : b.getName())
                .distinct()
                .collect(Collectors.joining(" · "));

        LocalDate effectiveLmp = (me.getFamily() != null) ? me.getFamily().getLmpDate() : me.getLmpDate();
        int week = 0;
        if (effectiveLmp != null) {
            week = (int) Math.max(0, ChronoUnit.WEEKS.between(effectiveLmp, LocalDate.now(KST)));
        }

        boolean hasUnread = hasUnreadLetterIcon(me);

        return HomeResponse.builder()
                .babyName(babyNames)
                .week(week)
                .hasUnreadLetters(hasUnread)
                .homeTheme(me.getHomeThemeOrDefault().name())
                .build();
    }

    private boolean hasUnreadLetterIcon(Member me) {
        if (me.getFamily() == null) return false;
        if (memberRepository.countByFamilyId(me.getFamily().getId()) <= 1) return false;
        //return !me.isHasSeenFamilyLetters();
        return true; // TODO : temp
    }

    @Transactional
    public TodayWriteResponse getTodayForWrite() {
        Member me = meWithFamily();

        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        Long famId = (me.getFamily() == null) ? null : me.getFamily().getId();

        //  질문 보장 + 조회 위임
        questionService.ensureTodayGlobalQuestion();
        DailyQuestion dq = questionService.getFor(today, famId);

        var mineOpt = letterRepository.findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                me.getId(), start, end);

        return TodayWriteResponse.builder()
                .date(today)
                .questionId(dq == null ? null : dq.getId())
                .questionText(dq == null ? null : dq.getQuestionText())
                .myLetterId(mineOpt.map(Letter::getId).orElse(null))
                .myLetterContent(mineOpt.map(Letter::getContent).orElse(null))
                .canWrite(mineOpt.isEmpty())
                .editable(mineOpt.isPresent())
                .build();
    }

    @Transactional(readOnly = true)
    public FeedResponse getFamilyFeed(String cursorIso, int size) {
        Member me = meWithFamily();
        Long meId = me.getId();
        Long familyId = (me.getFamily() == null) ? null : me.getFamily().getId();

        LocalDateTime cursor = (cursorIso == null || cursorIso.isBlank())
                ? null : LocalDateTime.parse(cursorIso);

        var pageable = PageRequest.of(0, Math.max(1, Math.min(size, 50)));

        List<Letter> letters = letterRepository.findFeedForUser(meId, familyId, cursor, pageable);

        var qCache = new HashMap<LocalDate, String>();
        var items = letters.stream().map(l -> {
            var d = l.getCreatedAt().toLocalDate();
            String qText = qCache.computeIfAbsent(d, dd -> questionService.getTextFor(dd, familyId));
            return LetterResponse.builder()
                    .id(l.getId())
                    .content(l.getContent())
                    .nickname(l.getWriter().getNickname())
                    .imgUrl(profileUrlOrDefault(l.getWriter()))
                    .createdAt(l.getCreatedAt())
                    .editable(l.getWriter().getId().equals(meId))
                    .question(qText)
                    .build();
        }).toList();

        String next = (letters.size() == pageable.getPageSize())
                ? letters.get(letters.size() - 1).getCreatedAt().toString()
                : null;

        return FeedResponse.builder()
                .items(items)
                .nextCursor(next)
                .build();
    }

    /*
     * 오늘 날짜에 대하여 읽지 않은 편지가 존재하는지 확인
     */
    @Transactional(readOnly = true)
    public Optional<LetterCheckResponseDto> getLetterCheck() {
        Long memberId = authenticatedProvider.getCurrentMemberId();
        LocalDate date = LocalDate.now(KST);
        return letterRepository.findOneByReceiverId(memberId, date);
    }

    // ==========================
    // 공통 유틸
    // ==========================
    private Member meWithFamily() {
        return memberRepository.findByIdWithFamily(authenticatedProvider.getCurrentMemberId())
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
    }

    private static void writeCookie(HttpServletResponse resp, String name, String value, int maxAge) {
        Cookie c = new Cookie(name, value);
        c.setPath("/");
        c.setHttpOnly(false);
        c.setMaxAge(maxAge);
        resp.addCookie(c);
    }

    private static String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        Optional<Cookie> found = Arrays.stream(req.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
        return found.map(Cookie::getValue).orElse(null);
    }

    @Transactional(readOnly = true)
    public ThemeResponse getTheme(HttpServletRequest req) {
        String cookieVal = readCookie(req, THEME_COOKIE);
        if (cookieVal != null && !cookieVal.isBlank()) {
            return new ThemeResponse(HomeTheme.fromOrDefault(cookieVal).name());
        }
        Long meId = null;
        try { meId = authenticatedProvider.getCurrentMemberId(); } catch (Exception ignored) {}
        if (meId != null) {
            var me = memberRepository.findById(meId)
                    .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
            return new ThemeResponse(me.getHomeThemeOrDefault().name());
        }
        return new ThemeResponse(HomeTheme.MINT.name());
    }

    @Transactional
    public ThemeResponse updateTheme(String requested, HttpServletRequest req, HttpServletResponse resp) {
        HomeTheme theme = HomeTheme.fromOrDefault(requested);
        writeCookie(resp, THEME_COOKIE, theme.name(), COOKIE_MAX_AGE);

        Long meId = null;
        try { meId = authenticatedProvider.getCurrentMemberId(); } catch (Exception ignored) {}
        if (meId != null) {
            var me = memberRepository.findById(meId)
                    .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
            me.changeHomeTheme(theme);
        }
        return new ThemeResponse(theme.name());
    }
}
