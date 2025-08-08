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
@Schema(description = "정보 목록에서 조회되는 DTO")
public class NewsPreviewResponseDto {

    private Long newsId;
    private String title;
    private String subTitle;
    private String imageUrl;
    private Category category;

    public static NewsPreviewResponseDto toPreviewDto(News news) {
        return NewsPreviewResponseDto.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .subTitle(news.getSubTitle())
                .category(news.getCategory())
                .imageUrl(news.getImgUrl())
                .build();
    }
}
