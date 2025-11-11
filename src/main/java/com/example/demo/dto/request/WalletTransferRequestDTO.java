package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import java.util.UUID;

@Data
public class WalletTransferRequestDTO {
    @NotNull
    private UUID fromWalletId;

    @NotNull
    private UUID toWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank // Để đảm bảo idempotency
    private String idempotencyKey; 
}