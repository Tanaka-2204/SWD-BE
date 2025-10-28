package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class EventBroadcastResponseDTO {

    private Long id;

    private Long eventId;

    private String eventTitle;

    private String messageContent;

    private OffsetDateTime sentAt;

    //private Long recipientCount; 
}