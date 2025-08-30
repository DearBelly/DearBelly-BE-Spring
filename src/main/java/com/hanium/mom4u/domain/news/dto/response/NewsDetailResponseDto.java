package com.hanium.mom4u.domain.news.dto.response;


import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "정보 상세 조회 응답 DTO")
public class NewsDetailResponseDto {

    private Long newsId;
    private String title;
    private String subTitle;
    private String content;
    private Category category;
    private String imageUrl;
    private String link;

    public static NewsDetailResponseDto toDetailDto(News news) {
        return NewsDetailResponseDto.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .subTitle(news.getSubTitle())
                .category(news.getCategory())
                .imageUrl(news.getImgUrl())
                .content(news.getContent())
                .link(news.getLink())
                .build();
    }
}
