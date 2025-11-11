package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class StudentBroadcastResponseDTO {
    
    private UUID deliveryId; 
    
    private String messageContent;
    private OffsetDateTime sentAt;
    private String status; // UNREAD | READ
    private UUID eventId;
    private String eventTitle;
}