package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_query_log")
public class AiQueryLog {
    @Id
    @ColumnDefault("nextval('ai_query_log_query_id_seq')")
    @Column(name = "query_id", nullable = false)
    private Long id;

    @Size(max = 20)
    @Column(name = "actor_type", length = 20)
    private String actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "question", length = Integer.MAX_VALUE)
    private String question;

    @Column(name = "answer", length = Integer.MAX_VALUE)
    private String answer;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}