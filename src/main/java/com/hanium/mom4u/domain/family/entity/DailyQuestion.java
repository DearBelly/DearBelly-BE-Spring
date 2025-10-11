package com.hanium.mom4u.domain.family.entity;
import com.hanium.mom4u.domain.common.BaseEntity;
import jakarta.persistence.*; import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "daily_question")
@Getter
@Setter
public class DailyQuestion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id") // NULL 허용(전역 질문)
    private Family family;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_question_id",
            foreignKey = @ForeignKey(name = "fk_dq_origin_question"))
    private Question originQuestion;

    @Column(name = "question_text", nullable = false, length = 500)
    private String dailyQuestionText;

    @Column(name = "answer")
    private String answer;

    @Column(name = "writer")
    private String writer;

    /** 기존 코드 호환용: getQuestionText()를 daily_question_text로 매핑 */
    public String getQuestionText() { return this.dailyQuestionText; } }