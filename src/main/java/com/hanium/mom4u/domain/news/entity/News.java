package com.hanium.mom4u.domain.news.entity;

import com.hanium.mom4u.domain.news.common.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "news")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class News {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Column(name = "title")
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name ="link")
    private String link;

    @Column(name = "img_url")
    private String imgUrl;
}
