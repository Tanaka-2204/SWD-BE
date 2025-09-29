package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "feedback_id")
    private UUID feedbackId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "sentiment_label")
    private String sentimentLabel;

    @Column(name = "sentiment_score")
    private Float sentimentScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}