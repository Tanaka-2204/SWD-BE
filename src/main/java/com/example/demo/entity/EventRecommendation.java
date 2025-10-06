package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "event_recommendation")
public class EventRecommendation {
    @Id
    @ColumnDefault("nextval('event_recommendation_rec_id_seq')")
    @Column(name = "rec_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private com.example.demo.entity.Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "score", precision = 6, scale = 3)
    private BigDecimal score;

    @Column(name = "explanation", length = Integer.MAX_VALUE)
    private String explanation;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}