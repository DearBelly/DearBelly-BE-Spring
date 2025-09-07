package com.hanium.mom4u.domain.news.entity;

import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;

@Entity
@Table(name="news_bookmark",
        uniqueConstraints=@UniqueConstraint(columnNames={"member_id","news_id"}))
public class NewsBookmark {
    @Id
    @GeneratedValue
    Long id;
    @ManyToOne
    Member member;
    @ManyToOne News news;
    protected NewsBookmark() {}
    public NewsBookmark(Member member, News news) {
        this.member = member;
        this.news = news;
    }
}

