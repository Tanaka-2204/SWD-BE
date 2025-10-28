package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class EventUpdateDTO {

    @Size(max = 200)
    private String title;

    private String description;

    private OffsetDateTime startTime;
    
    private OffsetDateTime endTime;

    @Size(max = 200)
    private String location;

    private Long categoryId;

    private BigDecimal rewardPerCheckin;

    private String status; // DRAFT | ACTIVE | FINISHED | CANCELLED
}