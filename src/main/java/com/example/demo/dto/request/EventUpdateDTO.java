package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;
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

    private UUID categoryId;
    
    // 1. Điểm cọc đăng ký (Cho phép cập nhật)
    @Min(value = 0, message = "Điểm cọc phải là số không âm.")
    private Integer pointCostToRegister; 

    // 2. Tổng điểm thưởng (Cho phép cập nhật)
    @Min(value = 0, message = "Tổng điểm thưởng phải là số không âm.")
    private Integer totalRewardPoints;
    
    // 3. Ngân sách (Cho phép cập nhật)
    private BigDecimal totalBudgetCoin;

    // XÓA: private BigDecimal rewardPerCheckin; 
    // =========================================================

    private String status; // DRAFT | ACTIVE | FINISHED | CANCELLED
}