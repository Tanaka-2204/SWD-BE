package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class WalletTransactionResponseDTO {
    private Long id;
    private Long walletId;
    private Long counterpartyId;
    private String txnType;
    private BigDecimal amount;
    private String referenceType;
    private Long referenceId;
    private OffsetDateTime createdAt;
}