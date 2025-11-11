package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import java.util.UUID;

@Data
public class EventFundingRequestDTO {
    @NotNull
    private UUID eventId;

    @NotNull
    @Positive
    private BigDecimal amount;
}