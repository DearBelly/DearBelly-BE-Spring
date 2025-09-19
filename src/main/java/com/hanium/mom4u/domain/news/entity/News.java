package com.hanium.mom4u.domain.news.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.news.common.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "news",
        indexes = {
                @Index(name="idx_post_category", columnList = "post_id, category")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uniq_post_category", columnNames = {"post_id", "category"})
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class News extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    @Column(name = "post_id")
    private Long postId;

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

    @Column(name = "posted_at")
    private LocalDate postedAt;

    @OneToMany(mappedBy = "news", orphanRemoval = true)
    private Set<NewsBookmark> newsBookmarks = new HashSet<>();
}
