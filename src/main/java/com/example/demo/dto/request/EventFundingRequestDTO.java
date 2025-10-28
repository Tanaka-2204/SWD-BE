package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class EventFundingRequestDTO {
    @NotNull
    private Long eventId;

    @NotNull
    @Positive
    private BigDecimal amount;
}