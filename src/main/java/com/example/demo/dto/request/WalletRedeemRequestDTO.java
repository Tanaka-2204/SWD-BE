package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import java.util.UUID;

@Data
public class WalletRedeemRequestDTO {
    @NotNull
    private UUID studentWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private UUID referenceId; // ID của ProductInvoice

    @NotBlank // Để đảm bảo idempotency
    private String idempotencyKey;
}