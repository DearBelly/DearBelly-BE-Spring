package com.hanium.mom4u.global.crawling.dto;

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
}
