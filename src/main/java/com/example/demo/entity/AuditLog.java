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
@Table(name = "auditlog")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_type", nullable = false)
    private String actorType;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "details", columnDefinition = "JSONB")
    private String details; // Use a custom converter for JSONB or map as String

    @Column(name = "created_at")
    private Timestamp createdAt;
}