package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EventResponseDTO {
    private UUID id;
    private UUID partnerId;
    private String partnerName;
    private String title;
    private String description;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String location;
    private EventCategoryResponseDTO category;
    private Integer pointCostToRegister; 
    private Integer totalRewardPoints;
    private BigDecimal totalBudgetCoin;
    private String status;
    private OffsetDateTime createdAt;
    private Integer maxAttendees;
}