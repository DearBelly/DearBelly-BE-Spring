package com.hanium.mom4u.domain.member.entity;

import com.hanium.mom4u.domain.calendar.entity.Schedule;
import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.news.entity.NewsBookmark;
import com.hanium.mom4u.domain.question.common.HomeTheme;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import com.hanium.mom4u.domain.news.common.Category;
import java.util.Set;
@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private SocialType socialType;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "is_pregnant")
    private boolean isPregnant;

    @Column(name = "lmp_date")
    private LocalDate lmpDate;

    @Column(name = "pre_pregnant")
    private Boolean prePregnant;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "is_inactive")
    private boolean isInactive;

    @Column(name = "inactive_date")
    private LocalDate inactiveDate;

    @Column(name = "is_light_mode")
    private Boolean isLightMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Baby> babyList;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "member", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<NewsBookmark> newsBookmarks = new HashSet<>();

    @ElementCollection(targetClass = Category.class)
    @CollectionTable(name = "member_interests", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Builder.Default
    private Set<Category> interests = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "home_theme", length = 20)
    private HomeTheme homeTheme;

    public HomeTheme getHomeThemeOrDefault() {
        return (this.homeTheme != null) ? this.homeTheme : HomeTheme.MINT;
    }
    public void changeHomeTheme(HomeTheme theme) {
        this.homeTheme = (theme != null) ? theme : HomeTheme.MINT;
    }
    public void assignFamily(Family family) {
        this.family = family;
    }



    public void inactive() {
        this.isInactive = true;
        this.inactiveDate = LocalDate.now();
    }


}


