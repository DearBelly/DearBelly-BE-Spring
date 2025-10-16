package com.hanium.mom4u.domain.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.dto.response.NewsDetailResponseDto;
import com.hanium.mom4u.domain.news.dto.response.NewsPreviewResponseDto;
import com.hanium.mom4u.domain.news.service.NewsService;
import com.hanium.mom4u.global.security.config.SecurityConfig;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;

import java.util.List;

import static com.hanium.mom4u.global.util.CustomRestDocsHandler.customDocument;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;


@ExtendWith(SpringExtension.class)
@WebMvcTest(
        controllers = com.hanium.mom4u.domain.news.controller.NewsController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,           // DataSource 제외
                JpaRepositoriesAutoConfiguration.class,      // JPA Repository 제외
                HibernateJpaAutoConfiguration.class          // Hibernate 제외
        }
)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "mom4u.hanium.com")
@Import({JwtAuthenticationFilter.class, SecurityConfig.class})
class NewsControllerTest{

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private NewsService newsService;

    @MockitoBean private AuthenticatedProvider authenticatedProvider;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
    }

    // 인증 헤더 스니펫
    private final HeaderDescriptor[] authHeader = new HeaderDescriptor[]{
            headerWithName("Authorization").description("Bearer {AccessToken}")
    };

    // Page 부분 응답
    private final FieldDescriptor[] pageMeta = new FieldDescriptor[] {
            fieldWithPath("page").type(NUMBER).description("현재 페이지"),
            fieldWithPath("size").type(NUMBER).description("페이지 사이즈"),
            fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지 존재 여부")
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
                .andDo(document(
                        "recommend",
                        responseFields(envelope)
                                .andWithPrefix("data[].",
                                        fieldWithPath("newsId").type(NUMBER).description("정보 ID"),
                                        fieldWithPath("title").type(STRING).description("제목"),
                                        fieldWithPath("subTitle").type(STRING).description("보조 제목"),
                                        fieldWithPath("imageUrl").type(STRING).description("대표 이미지 URL"),
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
                                        fieldWithPath("imageUrl").type(STRING).description("대표 이미지 URL"),
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
                        new String[]{"Authorization"},         // 요청에서 제거할 헤더
                        new String[]{"Set-Cookie"},            // 응답에서 제거할 헤더
                        pathParameters(parameterWithName("newsId").description("정보 ID")),
                        requestHeaders(authHeader),
                        responseFields(
                                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(NULL).description("본문 데이터(없음)")
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
                        new String[]{"Authorization"},
                        new String[]{"Set-Cookie"},
                        pathParameters(parameterWithName("newsId").description("정보 ID")),
                        requestHeaders(authHeader),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(NULL).description("본문 데이터(없음)")
                        )
                ));
    }
}