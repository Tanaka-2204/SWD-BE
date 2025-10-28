package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class EventCreateDTO {
    @NotNull
    private Long partnerId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    private String location;
    private Long categoryId;
    private BigDecimal rewardPerCheckin;
    private BigDecimal totalBudgetCoin;
}