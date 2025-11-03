package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

/**
 * DTO trả về thông tin chi tiết của một feedback.
 */
@Data
public class FeedbackResponseDTO {

    private Long id;

    private Long studentId;

    private String studentName;

    private Long eventId;

    private String eventTitle;

    private Short rating;

    private String comments;

    private String sentimentLabel;

    private OffsetDateTime createdAt;
}