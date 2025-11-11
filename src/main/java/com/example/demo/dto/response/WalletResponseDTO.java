package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class WalletResponseDTO {
    private UUID id;
    private String ownerType;
    private UUID ownerId;
    private String currency;
    private BigDecimal balance;
    private OffsetDateTime createdAt;
}