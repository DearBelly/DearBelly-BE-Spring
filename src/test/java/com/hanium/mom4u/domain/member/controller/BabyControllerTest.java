package com.hanium.mom4u.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.dto.request.BabyInfoRequestDto;
import com.hanium.mom4u.domain.member.dto.response.BabyInfoResponseDto;
import com.hanium.mom4u.domain.member.service.BabyService;
import com.hanium.mom4u.global.config.TestSecurityConfig;
import com.hanium.mom4u.global.security.jwt.AuthenticatedProvider;
import com.hanium.mom4u.global.security.jwt.JwtAuthenticationFilter;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.hanium.mom4u.global.util.CustomRestDocsHandler.customDocument;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(
        controllers = BabyController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.dearbelly.site")
@Import({JwtAuthenticationFilter.class, TestSecurityConfig.class})
class BabyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private BabyService babyService;
    @MockitoBean private AuthenticatedProvider authenticatedProvider;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @InjectMocks private BabyController babyController;

    private BabyInfoRequestDto babyInfoRequestDto;
    private BabyInfoResponseDto babyInfoResponseDto;

    @BeforeEach
    void setUp() {
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);

        babyInfoRequestDto = new BabyInfoRequestDto();
        babyInfoResponseDto = BabyInfoResponseDto.builder()
                .babyId(1L)
                .name("testBaby1")
                .babyGender(Gender.FEMALE)
                .lmpDate(LocalDate.of(2025, 1, 1))
                .currentWeek(38)
                .dueDateCalculated(LocalDate.of(2025, 10, 8))
                .build();
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

    @Test
    @DisplayName("[POST] /api/v1/baby - 아이 정보 등록")
    void saveBaby() throws Exception {
        // given
        babyInfoRequestDto = BabyInfoRequestDto.builder()
                .babyId(babyInfoResponseDto.getBabyId())
                .name(babyInfoResponseDto.getName())
                .babyGender(babyInfoResponseDto.getBabyGender())
                .build();
        given(babyService.saveBaby(any(BabyInfoRequestDto.class)))
                .willReturn(babyInfoResponseDto);

        // when & then
        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(STRING).description("응답 메시지"),
                fieldWithPath("data").type(OBJECT).description("뉴스 내용"),
                fieldWithPath("success").type(BOOLEAN).description("성공 여부")
        };

        mockMvc.perform(post("/api/v1/baby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(babyInfoRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "baby-save",
                        requestFields(
                                fieldWithPath("babyId").type(JsonFieldType.NUMBER).description("태아 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("태아 이름"),
                                fieldWithPath("babyGender").type(JsonFieldType.STRING).description("태아 성별 (MALE, FEMALE, UNKNOWN)")
                        ),
                        responseFields(envelope)
                                .andWithPrefix("data.",
                                        fieldWithPath("babyId").type(JsonFieldType.NUMBER).description("태아 ID"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("태아 이름"),
                                        fieldWithPath("babyGender").type(JsonFieldType.STRING).description("태아 성별 (MALE, FEMALE, UNKNOWN)"),
                                        fieldWithPath("lmpDate").type(JsonFieldType.STRING).description("최종 월경일 (yyyy.MM.dd)"),
                                        fieldWithPath("dueDateCalculated").type(JsonFieldType.STRING).description("출산 예정일 (yyyy-MM-dd)"),
                                        fieldWithPath("currentWeek").type(JsonFieldType.NUMBER).description("임신 주수")
                                )
                ));
    }

    @Test
    @DisplayName("[PATCH] /api/v1/baby/{babyId} - 태아 정보 수정 성공")
    void 태아정보_수정_성공() throws Exception {
        // given
        Long babyId = 1L;
        babyInfoRequestDto = BabyInfoRequestDto.builder()
                .babyId(babyId)
                .name("updateBaby")
                .babyGender(Gender.FEMALE)
                .build();

        BabyInfoResponseDto updatedResponse = BabyInfoResponseDto.builder()
                .babyId(babyId)
                .name(babyInfoRequestDto.getName())
                .babyGender(babyInfoRequestDto.getBabyGender())
                .lmpDate(LocalDate.of(2025, 1, 1))
                .currentWeek(38)
                .dueDateCalculated(LocalDate.of(2025, 10, 8))
                .build();

        given(babyService.updateBaby(eq(babyId), any(BabyInfoRequestDto.class)))
                .willReturn(updatedResponse);

        // when & then
        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description("태아 정보"),
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부")
        };

        mockMvc.perform(patch("/api/v1/baby/{babyId}", babyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(babyInfoRequestDto)) // Request PARSING
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "baby-update",
                        pathParameters(
                                parameterWithName("babyId").description("수정할 태아 ID")
                        ),
                        // Request Field 입력
                        requestFields(
                                fieldWithPath("babyId").type(JsonFieldType.NUMBER).description("ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("태아 이름"),
                                fieldWithPath("babyGender").type(JsonFieldType.STRING).description("태아 성별 (MALE, FEMALE, UNKNOWN)")
                        ),
                        // Response Field 응답으로 처리
                        responseFields(envelope)
                                .andWithPrefix("data.",
                                        fieldWithPath("babyId").type(JsonFieldType.NUMBER).description("태아 ID"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("태아 이름"),
                                        fieldWithPath("babyGender").type(JsonFieldType.STRING).description("태아 성별 (MALE, FEMALE, UNKNOWN)"),
                                        fieldWithPath("lmpDate").type(JsonFieldType.STRING).description("최종 월경일 (yyyy.MM.dd)"),
                                        fieldWithPath("dueDateCalculated").type(JsonFieldType.STRING).description("출산 예정일 (yyyy-MM-dd)"),
                                        fieldWithPath("currentWeek").type(JsonFieldType.NUMBER).description("임신 주수")
                                )
                ));
    }

    @Test
    @DisplayName("[GET] /api/v1/baby - 태아 정보 전체 조회 성공")
    void 태아정보_전체조회_성공() throws Exception {
        // given
        BabyInfoResponseDto babyInfoResponseDto2 = BabyInfoResponseDto.builder()
                .name("testBaby2")
                .babyGender(Gender.FEMALE)
                .lmpDate(LocalDate.of(2025, 1, 1))
                .currentWeek(40)
                .dueDateCalculated(LocalDate.of(2025, 10, 8))
                .build();

        List<BabyInfoResponseDto> babyList = List.of(babyInfoResponseDto, babyInfoResponseDto2);

        given(babyService.readAllBabyInfo()).willReturn(babyList);

        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                fieldWithPath("data").type(ARRAY).description("태아 정보"),
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부")
        };

        // when & then
        mockMvc.perform(get("/api/v1/baby")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "baby-get-all",
                        responseFields(envelope)
                                .andWithPrefix("data[].",
                                        fieldWithPath("babyId").type(JsonFieldType.NUMBER).description("태아 ID"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("태아 이름"),
                                        fieldWithPath("babyGender").type(JsonFieldType.STRING).description("태아 성별 (MALE, FEMALE, UNKNOWN)"),
                                        fieldWithPath("lmpDate").type(JsonFieldType.STRING).description("최종 월경일 (yyyy-MM-dd)").optional(),
                                        fieldWithPath("dueDateCalculated").type(JsonFieldType.STRING).description("출산 예정일 (yyyy-MM-dd)").optional(),
                                        fieldWithPath("currentWeek").type(JsonFieldType.NUMBER).description("임신 주수").optional()
                                )
                ));
    }

    @Test
    @DisplayName("[GET] /api/v1/baby/{babyId} - 특정 태아 정보 조회 성공")
    void 특정_태아정보_조회_성공() throws Exception {
        // given
        Long babyId = 1L;
        given(babyService.readBabyInfo(babyId)).willReturn(babyInfoResponseDto);

        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                fieldWithPath("data").type(OBJECT).description("태아 정보 데이터"),
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부")
        };

        // when & then
        mockMvc.perform(get("/api/v1/baby/{babyId}", babyId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "baby-get-by-id",
                        pathParameters(
                                parameterWithName("babyId").description("태아 ID")
                        ),
                        responseFields(envelope)
                                .andWithPrefix("data.",
                                        fieldWithPath("babyId").type(JsonFieldType.NUMBER).description("태아 ID"),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("태아 이름"),
                                        fieldWithPath("babyGender").type(JsonFieldType.STRING).description("태아 성별 (MALE, FEMALE, UNKNOWN)"),
                                        fieldWithPath("lmpDate").type(JsonFieldType.STRING).description("최종 월경일 (yyyy-MM-dd)").optional(),
                                        fieldWithPath("dueDateCalculated").type(JsonFieldType.STRING).description("출산 예정일 (yyyy-MM-dd)").optional(),
                                        fieldWithPath("currentWeek").type(JsonFieldType.NUMBER).description("임신 주수").optional()
                                )
                ));
    }

    @Test
    @DisplayName("[DELETE] /api/v1/baby/{babyId} - 태아 정보 삭제 성공")
    void 태아정보_삭제_성공() throws Exception {
        // given
        Long babyId = 1L;
        doNothing().when(babyService).deleteBaby(babyId);

        FieldDescriptor[] envelope = new FieldDescriptor[]{
                fieldWithPath("httpStatus").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부")
        };

        // when & then
        mockMvc.perform(delete("/api/v1/baby/{babyId}", babyId))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(customDocument(
                        "baby-delete",
                        pathParameters(
                                parameterWithName("babyId").description("태아 ID")
                        ),
                        responseFields(envelope)
                ));
    }
}