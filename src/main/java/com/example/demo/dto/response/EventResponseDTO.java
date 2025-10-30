package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class EventResponseDTO {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private String title;
    private String description;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String location;
    private EventCategoryResponseDTO category;
    private BigDecimal rewardPerCheckin;
    private BigDecimal totalBudgetCoin;
    private String status;
    private OffsetDateTime createdAt;
    private Integer maxAttendees;
}