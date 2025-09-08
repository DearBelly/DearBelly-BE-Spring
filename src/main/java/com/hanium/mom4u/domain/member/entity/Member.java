package com.hanium.mom4u.domain.member.entity;

import com.hanium.mom4u.domain.calendar.entity.Schedule;
import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.family.entity.Family;
import com.hanium.mom4u.domain.member.common.Gender;
import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.common.SocialType;
import com.hanium.mom4u.domain.news.entity.NewsBookmark;
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

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    @OneToMany(mappedBy = "member")
    private List<Baby> babyList;

    @OneToMany(mappedBy = "member")
    private List<Schedule> scheduleList;

    @Column(name = "has_seen_family_letters", nullable = false)
    private boolean hasSeenFamilyLetters = false;

    @OneToMany(mappedBy = "member")
    private Set<NewsBookmark> newsBookmarks = new HashSet<>();

    @ElementCollection(targetClass = Category.class)
    @CollectionTable(name = "member_interests", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    @Builder.Default
    private Set<Category> interests = new HashSet<>();



    public void assignFamily(Family family) {
        this.family = family;
    }



    public void inactive() {
        this.isInactive = true;
        this.inactiveDate = LocalDate.now();
    }


}


