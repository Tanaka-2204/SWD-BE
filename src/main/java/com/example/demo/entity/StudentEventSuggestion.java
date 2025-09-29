package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "studenteventsuggestion")
public class StudentEventSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "suggestion_id")
    private UUID suggestionId;

    @Column(name = "suggestion_score")
    private Float suggestionScore;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}