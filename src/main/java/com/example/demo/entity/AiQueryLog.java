package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_query_log")
public class AiQueryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SỬA ĐỔI NÀY
    @Column(name = "query_id", nullable = false)
    private Long id;

    @Size(max = 20)
    @Column(name = "actor_type", length = 20)
    private String actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "question") // Chỉ định tên cột là đủ
    private String question;

    @Column(name = "answer")
    private String answer;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}