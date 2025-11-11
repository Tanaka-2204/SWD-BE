package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class WalletTransactionResponseDTO {
    private UUID id;
    private UUID walletId;
    private UUID counterpartyId;
    private String txnType;
    private BigDecimal amount;
    private String referenceType;
    private UUID referenceId;
    private OffsetDateTime createdAt;
}