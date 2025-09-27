package com.hanium.mom4u.domain.question.service;

import com.hanium.mom4u.domain.family.entity.DailyQuestion;
import com.hanium.mom4u.domain.family.repository.FamilyRepository;
import com.hanium.mom4u.domain.member.entity.Baby;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.BabyRepository;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.domain.question.common.HomeTheme;
import com.hanium.mom4u.domain.question.dto.request.LetterRequest;
import com.hanium.mom4u.domain.question.dto.response.*;
import com.hanium.mom4u.domain.question.entity.Letter;
import com.hanium.mom4u.domain.question.repository.DailyQuestionRepository;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterService {

    private final LetterRepository letterRepository;
    private final AuthenticatedProvider authenticatedProvider;
    private final BabyRepository babyRepository;
    private final MemberRepository memberRepository;
    private final DailyQuestionRepository dailyQuestionRepository;


    private final MessagePublisher messagePublisher;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final String THEME_COOKIE = "HOME_THEME";
    private final FamilyRepository familyRepository;
    @Value("${spring.home.theme.cookie}")
    private int COOKIE_MAX_AGE ;

    @Value("${spring.cloud.aws.s3.default-image}")
    private String DEFAULT_IMAGE;

    private String profileUrlOrDefault(Member m) {
        String url = (m == null) ? null : m.getImgUrl();
        return (url == null || url.isBlank()) ? DEFAULT_IMAGE : url;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void assignGlobalAtMidnight() {
        ensureTodayGlobalQuestion();
    }

    /*
    편지 작성하기
     */
    @Transactional
    public void create(LetterRequest req) {

        Long memberId = authenticatedProvider.getCurrentMemberId();
        // fetch join을 통하여 미리 가족 정보까지 가져옴
        Member me = memberRepository.findWithFamilyAndMembers(memberId)
                .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));

        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        LocalDate today = LocalDate.now(KST);

        if (letterRepository.findOneByReceiverId(memberId, today).isEmpty()) {
            log.error("이미 작성된 편지 조회...");
            throw GeneralException.of(StatusCode.LETTER_TODAY_ALREADY_WRITTEN);
        }

        List<Member> memberList = me.getFamily().getMemberList();

        // 가족 구성원의 수만큼 편지 저장
        List<Letter> letterList = new ArrayList<>();
        for (Member member: memberList) {
            Letter letter = Letter.builder()
                    .content(content)
                    .writer(me)
                    .family(me.getFamily()) // null 가능
                    .receiver(member) // 다른 사람
                    .build();

            letterList.add(letter);
        }
        letterRepository.saveAll(letterList);

        if (me.getFamily() != null) {
            letterRepository.resetSeenFlagForFamilyExceptWriter(me.getFamily().getId(), me.getId());
            letterRepository.markSeenForMember(me.getId());
            List<Long> memberIdList = me.getFamily().getMemberList().stream()
                    .map(Member::getId)
                    .filter(Predicate.not(me.getId()::equals))
                    .toList();

            for (Long receiverId : memberIdList) {
                messagePublisher.publish(
                        "Alarm",
                        MessageDto.builder()
                                .receiverId(receiverId)
                                .title("새로운 편지가 도착했어요!")
                                .content("새로운 편지가 도착했어요! 확인해보세요!")
                                .build()
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<LetterResponse> getByMonth(Integer year, Integer month) {
        Member me = meWithFamily();
        YearMonth ym = (year == null || month == null) ? YearMonth.now(KST) : YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Letter> letters;
        if (me.getFamily() != null) {
            letters = letterRepository.findByFamilyAndCreatedAtBetween(me.getFamily(), start, end);
        } else {
            letters = letterRepository.findByWriterAndCreatedAtBetween(me, start, end);
        }

        return letters.stream().map(l -> {
            Long famId = (me.getFamily() == null) ? null : me.getFamily().getId();
            String qText = getQuestionTextFor(l.getCreatedAt().toLocalDate(), famId);
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

    @Transactional
    public void update(Long letterId, LetterRequest req) {
        Member me = meWithFamily();
        Long myId = me.getId();
        String content = req.getContent().trim();
        if (content.isEmpty()) throw GeneralException.of(StatusCode.LETTER_CONTENT_REQUIRED);
        if (content.length() > 300) throw GeneralException.of(StatusCode.LETTER_CONTENT_TOO_LONG);

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));
        if (!letter.getWriter().getId().equals(myId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        letter.updateContent(content);

        if (letter.getFamily() != null) {
            letterRepository.resetSeenFlagForFamilyExceptWriter(letter.getFamily().getId(), myId);
            letterRepository.markSeenForMember(myId);
        }
    }

    public void delete(Long letterId) {
        Long myId = authenticatedProvider.getCurrentMemberId();
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> GeneralException.of(StatusCode.LETTER_NOT_FOUND));
        if (!letter.getWriter().getId().equals(myId))
            throw GeneralException.of(StatusCode.LETTER_FORBIDDEN);

        letterRepository.delete(letter);
    }

    /*
    편지 읽기
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

        // 남이 쓴 가족 편지면 읽음 처리
        boolean shouldMarkSeen = !isMine && (sameByLetterFamily || sameByWriterFamily);
        if (shouldMarkSeen) {
            letterRepository.markSeenForMember(me.getId());
        }


        Long famId = (me.getFamily() == null) ? null : me.getFamily().getId();
        String qText = getQuestionTextFor(letter.getCreatedAt().toLocalDate(), famId);

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
        return !me.isHasSeenFamilyLetters();
    }

    @Transactional
    public TodayWriteResponse getTodayForWrite() {
        Member me = meWithFamily();

        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        Long famId = (me.getFamily() == null) ? null : me.getFamily().getId();
        DailyQuestion dq = getQuestionFor(today, famId);

        var mineOpt = letterRepository.findTopByWriter_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                me.getId(), start, end);

        return TodayWriteResponse.builder()
                .date(today)
                .questionId(dq == null ? null : dq.getId())
                .questionContent(dq == null ? null : dq.getQuestion())
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
            String qText = qCache.computeIfAbsent(d, dd -> getQuestionTextFor(dd, familyId));
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
    오늘 날짜에 대하여 읽지 않은 편지가 존재하는지에 대해 확인
     */
    @Transactional(readOnly = true)
    public Optional<LetterCheckResponseDto> getLetterCheck() {
        Long memberId = authenticatedProvider.getCurrentMemberId();
        LocalDate date = LocalDate.now();

        return letterRepository.findOneByReceiverId(memberId, date);
    }

    // === 질문 보장/조회 헬퍼 ===

    private DailyQuestion getQuestionFor(LocalDate date, Long familyId) {
        if (date.equals(LocalDate.now(KST))) {
            ensureTodayGlobalQuestion();
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        Pageable one = PageRequest.of(0, 1);

        if (familyId != null) {
            return dailyQuestionRepository.findLatestForFamily(familyId, start, end, one)
                    .stream().findFirst()
                    .orElseGet(() -> dailyQuestionRepository.findLatestGlobal(start, end, one)
                            .stream().findFirst().orElse(null));
        }
        return dailyQuestionRepository.findLatestGlobal(start, end, one)
                .stream().findFirst().orElse(null);
    }

    private void ensureTodayGlobalQuestion() {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        if (dailyQuestionRepository.existsByFamilyIsNullAndCreatedAtBetween(start, end)) {
            return;
        }

        LocalDate yesterday = today.minusDays(1);
        LocalDateTime yStart = yesterday.atStartOfDay();
        LocalDateTime yEnd = yStart.plusDays(1).minusNanos(1);
        String yesterdayContent = dailyQuestionRepository
                .findLatestGlobal(yStart, yEnd, PageRequest.of(0, 1))
                .stream().findFirst().map(DailyQuestion::getQuestion).orElse(null);

        var pickOpt = dailyQuestionRepository.pickRandomQuestionExcludingContent(yesterdayContent);
        var pick = pickOpt.orElseGet(() -> dailyQuestionRepository.pickAnyQuestion()
                .orElseThrow(() -> new IllegalStateException("No questions found")));

        DailyQuestion created = new DailyQuestion();
        created.setFamily(null);
        created.setQuestion(pick.getContent());
        created.setQuestionId(pick.getId());

        dailyQuestionRepository.save(created);
    }

    private String getQuestionTextFor(LocalDate date, Long familyId) {
        DailyQuestion dq = getQuestionFor(date, familyId);
        return (dq == null) ? null : dq.getQuestion();
    }

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
