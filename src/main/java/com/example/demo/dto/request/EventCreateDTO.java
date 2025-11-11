package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EventCreateDTO {
    @NotNull
    private UUID partnerId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;

    private String location;
    private UUID categoryId;

    @NotNull(message = "Điểm cọc không được để trống.")
    @Min(value = 0, message = "Điểm cọc phải là số không âm.")
    private Integer pointCostToRegister; 
    
    @NotNull(message = "Tổng điểm thưởng không được để trống.")
    @Min(value = 0, message = "Tổng điểm thưởng phải là số không âm.")
    private Integer totalRewardPoints;
    
    private BigDecimal totalBudgetCoin;
}