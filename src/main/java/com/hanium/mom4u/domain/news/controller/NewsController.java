package com.hanium.mom4u.domain.news.controller;

import com.hanium.mom4u.domain.news.service.NewsService;
import com.hanium.mom4u.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "정보 API Controller", description = "정보 관련 API Controller입니다")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    // 정보 추천 별 보여주기(3개)
    @Operation(summary = "추천 정보 반환 API", description = """
            사용자가 관심있는 카테고리를 기반으로 추천 글을 불러오는 API입니다.<br>
            기본 반환 개수는 3개입니다.<br>
            만약 반환 카테고리가 지정되어있지 않다면 랜덤입니다.<br>
            """)
    @GetMapping()
    public ResponseEntity<CommonResponse<?>> recommend() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(
                        newsService.getRecommend()
                )
        );
    }

    // 정보 카테고리 별 대표 하나씩 보여주기
    @Operation(summary = "각 카테고리 별 반환 API", description = """
            각 카테고리 별로 한 개 씩 게시글들을 반환하는 API입니다.<br>
            카테고리 별로 대표 글 하나씩 반환됩니다.<br>
            """)
    @GetMapping("/category")
    public ResponseEntity<CommonResponse<?>> getEachCategory() {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(
                        newsService.getNewsPerCategory()
                )
        );
    }

    // 정보 카테고리 별 보여주기 및 전체 보여주기
    @Operation(summary = "각 카테고리 별로 전체 조회 API", description = """
            각 카테고리 별로 정보 글에 대하여 전체를 조회하는 API입니다.<br>
            무한스크롤 기반이므로 page 시작은 기본 0입니다.<br>
            Order에 조회할 카테고리를 입력해주세요.<br>
            0: 전체, 1: 건강, 2: 지원금, 3: 임신준비, 4:출산/육아, 5:정서 
            """)
    @GetMapping("/category/{categoryOrder}")
    public ResponseEntity<CommonResponse<?>> getAllByCategory(
            @PathVariable("categoryOrder") int index,
            @RequestParam("page") int page
    ) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(
                        newsService.getAllNewsPerCategory(index, page)
                )
        );
    }

    @Operation(summary = "정보 상세 조회 API", description = "Path 값 안에 newsId를 입력해주세요.")
    @GetMapping("/{newsId}")
    public ResponseEntity<CommonResponse<?>> getDetail(
            @PathVariable("newsId") Long newsId
    ) {
        return ResponseEntity.ok(
                CommonResponse.onSuccess(
                        newsService.getDetail(newsId)
                )
        );
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{newsId}/bookmark")
    @Operation(summary = "북마크 추가", description = "해당 정보를 내 북마크에 추가합니다.")
    public ResponseEntity<CommonResponse<?>> addBookmark(@PathVariable Long newsId) {
        newsService.addBookmark(newsId);
        return ResponseEntity.ok(CommonResponse.onSuccess(null));
    }
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{newsId}/bookmark")
    @Operation(summary = "북마크 해제", description = "해당 정보의 북마크를 해제합니다.")
    public ResponseEntity<CommonResponse<?>> removeBookmark(@PathVariable Long newsId) {
        newsService.removeBookmark(newsId);
        return ResponseEntity.ok(CommonResponse.onSuccess(null));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/bookmarks")
    @Operation(summary = "내 북마크 목록", description = "로그인 사용자의 북마크한 정보 목록을 조회합니다.")
    public ResponseEntity<CommonResponse<?>> myBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(CommonResponse.onSuccess(newsService.getMyBookmarks(page, size)));
    }
}
