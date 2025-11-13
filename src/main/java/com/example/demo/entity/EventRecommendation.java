package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "event_recommendation")
public class EventRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // SỬA ĐỔI CHIẾN LƯỢC
    @Column(name = "rec_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "score", precision = 6, scale = 3)
    private BigDecimal score;

    @Column(name = "explanation") 
    private String explanation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

}