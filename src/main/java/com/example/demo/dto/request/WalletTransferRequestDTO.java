package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class WalletTransferRequestDTO {
    @NotNull
    private Long fromWalletId;

    @NotNull
    private Long toWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank // Để đảm bảo idempotency
    private String idempotencyKey; 
}