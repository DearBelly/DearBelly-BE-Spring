package com.hanium.mom4u.domain.news.entity;

import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;

@Entity
@Table(name="news_bookmark",
        uniqueConstraints=@UniqueConstraint(columnNames={"member_id","news_id"})) // 이 부분은 아래와 같이 수정될 겁니다.
public class NewsBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    News news;

    protected NewsBookmark() {}
    public NewsBookmark(Member member, News news) {
        this.member = member;
        this.news = news;
    }
}