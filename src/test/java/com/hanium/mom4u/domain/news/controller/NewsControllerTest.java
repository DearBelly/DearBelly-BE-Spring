package com.hanium.mom4u.domain.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.dto.response.NewsDetailResponseDto;
import com.hanium.mom4u.domain.news.dto.response.NewsPreviewResponseDto;
import com.hanium.mom4u.domain.news.service.NewsService;
import com.hanium.mom4u.global.config.TestSecurityConfig;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import com.hanium.mom4u.global.security.jwt.JwtAuthenticationFilter;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;

import java.util.ArrayList;
import java.util.List;

import static com.hanium.mom4u.global.util.CustomRestDocsHandler.customDocument;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;


@ExtendWith(SpringExtension.class)
@WebMvcTest(
        controllers = com.hanium.mom4u.domain.news.controller.NewsController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.dearbelly.site")
@Import({JwtAuthenticationFilter.class, TestSecurityConfig.class})
class NewsControllerTest{

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private NewsService newsService;

    @MockitoBean private AuthenticatedProvider authenticatedProvider;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // JWT 토큰 파싱 및 인증 설정 Mock
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
    }

    // 인증 헤더 스니펫
    private static final HeaderDescriptor authHeader = headerWithName("Authorization")
            .description("Bearer Access Token")
            .attributes(key("format").value("Bearer {access-token}"));


    // Page 부분 응답
    private final FieldDescriptor[] pageMeta = new FieldDescriptor[] {
            fieldWithPath("page").type(NUMBER).description("현재 페이지"),
            fieldWithPath("size").type(NUMBER).description("페이지 사이즈")
    };

    /**
     * 추천 정보 반환
     */
    @Test
    @DisplayName("[GET] /api/v1/news - 추천 정보 반환")
    void recommend() throws Exception {

        // given
        List<NewsPreviewResponseDto> newsPreviewResponseDtoList =
                List.of(
                        new NewsPreviewResponseDto(1L, "title1", "SubTitle1", "image1.png", Category.HEALTH, false),
                        new NewsPreviewResponseDto(2L, "title2", "SubTitle2", "image2.png", Category.FINANCIAL, false),
                        new NewsPreviewResponseDto(3L, "title3", "SubTitle3", "image3.png", Category.HEALTH, false)
                        )
                ;

        // when
        given(newsService.getRecommend()).willReturn(newsPreviewResponseDtoList);

        // then
        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(STRING).description("응답 메시지"),
                fieldWithPath("data").type(ARRAY).description("뉴스 목록"),
                fieldWithPath("success").type(BOOLEAN).description("성공 여부")
        };

        mockMvc.perform(get("/api/v1/news").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "recommend",
                        responseFields(envelope)
                                .andWithPrefix("data[].",
                                        fieldWithPath("newsId").type(NUMBER).description("정보 ID"),
                                        fieldWithPath("title").type(STRING).description("제목"),
                                        fieldWithPath("subTitle").type(STRING).description("보조 제목"),
                                        fieldWithPath("imgUrl").type(STRING).description("대표 이미지 URL"),
                                        fieldWithPath("category").type(STRING).description("카테고리"),
                                        fieldWithPath("bookmarked").type(BOOLEAN).description("북마크 여부")
                                )
                ));
    }


    @Test
    @DisplayName("[GET] /api/v1/news/{categoryOrder} - 카테고리 별 뉴스 반환")
    void getAll() throws Exception {
        // given
        int categoryOrder = 1;
        int page = 0;
        int size = 15;
        int dataLength = 20;

        List<NewsPreviewResponseDto> newsList = new ArrayList<>();
        Long newsId = 1L;
        for (int i = 0; i < dataLength; i++) {
            newsList.add(new NewsPreviewResponseDto(newsId + i,
                    "title" + i,
                    "SubTitle" + i,
                    "image" + i + ".png",
                    Category.HEALTH,
                    false));
        }
        Pageable pageable = PageRequest.of(page, size);
        Slice<NewsPreviewResponseDto> newsListSlice = new SliceImpl<>(newsList, pageable, true);

        // when
        given(newsService.getAllNewsPerCategory(categoryOrder, page)).willReturn(newsListSlice);

        // then
        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(STRING).description("응답 메시지"),
                fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                fieldWithPath("success").type(BOOLEAN).description("성공 여부")
        };

        mockMvc.perform(get("/api/v1/news/category/{categoryOrder}", categoryOrder)
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(dataLength))
                .andExpect(jsonPath("$.data.size").value(size))
                .andExpect(jsonPath("$.data.number").value(page))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false))
                .andDo(customDocument(
                        "get-news-by-category",
                        pathParameters(
                                parameterWithName("categoryOrder").description("카테고리 순서 (인덱스)")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호")
                        ),
                        responseFields(envelope)
                                .andWithPrefix("data.",
                                        fieldWithPath("content").type(ARRAY).description("뉴스 목록"),
                                        fieldWithPath("size").type(NUMBER).description("페이지 크기"),
                                        fieldWithPath("number").type(NUMBER).description("현재 페이지 번호"),
                                        fieldWithPath("numberOfElements").type(NUMBER).description("현재 페이지의 요소 수"),
                                        fieldWithPath("first").type(BOOLEAN).description("첫 페이지 여부"),
                                        fieldWithPath("last").type(BOOLEAN).description("마지막 페이지 여부"),
                                        fieldWithPath("empty").type(BOOLEAN).description("비어있는지 여부"),
                                        fieldWithPath("sort").type(OBJECT).description("정렬 정보"),
                                        fieldWithPath("sort.empty").type(BOOLEAN).description("정렬 정보 비어있는지 여부"),
                                        fieldWithPath("sort.sorted").type(BOOLEAN).description("정렬 여부"),
                                        fieldWithPath("sort.unsorted").type(BOOLEAN).description("비정렬 여부"),
                                        fieldWithPath("pageable").type(OBJECT).description("페이지 정보"),
                                        fieldWithPath("pageable.offset").type(NUMBER).description("오프셋"),
                                        fieldWithPath("pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                                        fieldWithPath("pageable.pageSize").type(NUMBER).description("페이지 크기"),
                                        fieldWithPath("pageable.paged").type(BOOLEAN).description("페이징 여부"),
                                        fieldWithPath("pageable.unpaged").type(BOOLEAN).description("비페이징 여부"),
                                        fieldWithPath("pageable.sort").type(OBJECT).description("정렬 정보"),
                                        fieldWithPath("pageable.sort.empty").type(BOOLEAN).description("정렬 정보 비어있는지 여부"),
                                        fieldWithPath("pageable.sort.sorted").type(BOOLEAN).description("정렬 여부"),
                                        fieldWithPath("pageable.sort.unsorted").type(BOOLEAN).description("비정렬 여부")
                                )
                                .andWithPrefix("data.content[].",
                                        fieldWithPath("newsId").type(NUMBER).description("정보 ID"),
                                        fieldWithPath("title").type(STRING).description("제목"),
                                        fieldWithPath("subTitle").type(STRING).description("보조 제목"),
                                        fieldWithPath("imgUrl").type(STRING).description("대표 이미지 URL"),
                                        fieldWithPath("category").type(STRING).description("카테고리"),
                                        fieldWithPath("bookmarked").type(BOOLEAN).description("북마크 여부")
                                )
                ));
    }

    /**
     * 특정 게시물 상세 조회
     */
    @Test
    @DisplayName("[GET] /api/v1/news/{newsId} - 상세 조회")
    void getDetail() throws Exception {

        // given
        NewsDetailResponseDto detail = new NewsDetailResponseDto(
                777L, "초보 부모를 위한 출산 준비",
                "초보 부모를 위한 출산 준비는 어떻게 할까요?",
                "본문 내용...",
                Category.PREGNANCY_PLANNING,
                "image1.png",
                "https://originalLink",
                false
        );
        // when
        when(newsService.getDetail(anyLong())).thenReturn(detail);

        // then
        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(STRING).description("응답 메시지"),
                fieldWithPath("data").type(OBJECT).description("뉴스 내용"),
                fieldWithPath("success").type(BOOLEAN).description("성공 여부")
        };

        mockMvc.perform(get("/api/v1/news/{newsId}", 777L).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "detail",
                        pathParameters(parameterWithName("newsId").description("정보 ID")),
                        responseFields(envelope)
                                .andWithPrefix("data.",
                                        fieldWithPath("newsId").type(NUMBER).description("정보 ID"),
                                        fieldWithPath("title").type(STRING).description("제목"),
                                        fieldWithPath("subTitle").type(STRING).description("보조 제목"),
                                        fieldWithPath("content").type(STRING).description("내용"),
                                        fieldWithPath("category").type(STRING).description("카테고리"),
                                        fieldWithPath("imgUrl").type(STRING).description("대표 이미지 URL"),
                                        fieldWithPath("link").type(STRING).description("원문 링크"),
                                        fieldWithPath("bookmarked").type(BOOLEAN).description("북마크 여부")
                                )
                ));
    }

    /**
     * 북마크 추가
     */
    @Test
    @DisplayName("[PUT] /api/v1/news/{newsId}/bookmark - 북마크 추가")
    void addBookmark() throws Exception {

        Mockito.doNothing().when(newsService).addBookmark(anyLong());

        mockMvc.perform(put("/api/v1/news/{newsId}/bookmark", 10L)
                        .header("Authorization", "Bearer access-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "bookmark-add",
                        pathParameters(parameterWithName("newsId").description("정보 ID")),
                        requestHeaders(authHeader),
                        responseFields(
                                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지")
                        )
                ));
    }

    /**
     * 북마크 해제하기
     */
    @Test
    @DisplayName("[DELETE] /api/v1/news/{newsId}/bookmark - 북마크 해제")
    void removeBookmark() throws Exception {
        Mockito.doNothing().when(newsService).removeBookmark(anyLong());

        mockMvc.perform(delete("/api/v1/news/{newsId}/bookmark", 10L)
                        .header("Authorization", "Bearer access-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "bookmark-remove",
                        pathParameters(parameterWithName("newsId").description("정보 ID")),
                        requestHeaders(authHeader),
                        responseFields(
                                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지")
                        )
                ));
    }
}