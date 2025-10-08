package com.hanium.mom4u.domain.letter.entity;

import com.hanium.mom4u.domain.common.BaseEntity;
import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
회원이 각각의 편지에 대하여 읽었는지에 대한 Entity
 */
@Entity
@Table(name = "letter_read",
uniqueConstraints = @UniqueConstraint(columnNames = {"letter_id", "member_id"}),
        indexes = {
                @Index(name = "idx_read_member_time", columnList = "member_id, read_at"),
                @Index(name = "idx_read_letter", columnList = "letter_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
public class LetterRead extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "letter_read_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "letter_id", foreignKey = @ForeignKey(name = "fk_read_letter"))
    private Letter letter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_read_member"))
    private Member member;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
