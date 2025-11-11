package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class WalletTopupRequestDTO {
    @NotNull
    private UUID partnerId;

    @NotNull
    @Positive
    private BigDecimal amount;
}