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
@Table(name = "studentnotificationpreferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "notification_type"}))
public class StudentNotificationPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "preference_id")
    private UUID preferenceId;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "email_enabled")
    private Boolean emailEnabled;

    @Column(name = "push_enabled")
    private Boolean pushEnabled;

    @Column(name = "in_app_enabled")
    private Boolean inAppEnabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}