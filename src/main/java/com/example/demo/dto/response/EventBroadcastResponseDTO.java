package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EventBroadcastResponseDTO {

    private UUID id;

    private UUID eventId;

    private String eventTitle;

    private String messageContent;

    private OffsetDateTime sentAt;
}