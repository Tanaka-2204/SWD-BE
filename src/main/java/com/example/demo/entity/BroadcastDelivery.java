package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import com.example.demo.entity.enums.BroadcastDeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "broadcast_delivery")
public class BroadcastDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // SỬA ĐỔI CHIẾN LƯỢC
    @Column(name = "delivery_id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "broadcast_id", nullable = false)
    private EventBroadcast broadcast;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'UNREAD'")
    @Column(name = "status", nullable = false, length = 20)
    private BroadcastDeliveryStatus status = BroadcastDeliveryStatus.UNREAD;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}