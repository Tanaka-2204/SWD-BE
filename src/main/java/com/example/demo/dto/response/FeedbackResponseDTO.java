package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class FeedbackResponseDTO {

    private UUID id;

    private UUID studentId;

    private String studentName;

    private UUID eventId;

    private String eventTitle;

    private Short rating;

    private String comments;

    private String sentimentLabel;

    private OffsetDateTime createdAt;
}