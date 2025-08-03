package com.hanium.mom4u.global.crawling.dto;

import com.hanium.mom4u.domain.news.common.Category;
import com.hanium.mom4u.domain.news.entity.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlingResultDto {

    private Long postId;
    private String contentType;
    private String title;
    private String subTitle;
    private String link;
    private String content;
    private String imageUrl;
    private String postedAt;

    public static News toEntity(CrawlingResultDto dto) {
        return News.builder()
                .category(Category.valueOf(dto.getContentType()))
                .title(dto.getTitle())
                .subTitle(dto.getSubTitle())
                .content(dto.getContent())
                .link(dto.getLink())
                .imgUrl(dto.getImageUrl())
                .build();
    }
}
