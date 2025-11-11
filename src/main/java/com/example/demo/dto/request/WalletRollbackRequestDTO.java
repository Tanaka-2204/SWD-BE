package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class WalletRollbackRequestDTO {
    @NotNull
    private UUID originalTransactionId; // ID của giao dịch gốc cần hoàn lại

    @NotBlank // Để đảm bảo idempotency
    private String idempotencyKey;
    
    private String reason; // Lý do hoàn tiền
}