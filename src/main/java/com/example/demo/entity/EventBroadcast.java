package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.util.UUID;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "event_broadcast")
public class EventBroadcast {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // SỬA ĐỔI CHIẾN LƯỢC
    @Column(name = "broadcast_id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @Column(name = "message_content", nullable = false)
    private String messageContent;

    @ColumnDefault("now()")
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

}