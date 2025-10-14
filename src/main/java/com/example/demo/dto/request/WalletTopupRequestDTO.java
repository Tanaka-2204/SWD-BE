package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class WalletTopupRequestDTO {
    @NotNull
    private Long partnerId;

    @NotNull
    @Positive
    private BigDecimal amount;
}