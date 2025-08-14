package com.hanium.mom4u.domain.question.entity;

import com.hanium.mom4u.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
@Builder
@Getter
@Entity
@Table(name="letter_read",
        uniqueConstraints=@UniqueConstraint(columnNames={"letter_id","reader_id"}))
@NoArgsConstructor
@AllArgsConstructor
public class LetterRead {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch= LAZY, optional=false) @JoinColumn(name="letter_id")
    private Letter letter;

    @ManyToOne(fetch=LAZY, optional=false) @JoinColumn(name="reader_id")
    private Member reader;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    public static LetterRead of(Letter letter, Member reader) {
        return LetterRead.builder()
                .letter(letter)
                .reader(reader)
                .readAt(LocalDateTime.now())
                .build();
    }
}
