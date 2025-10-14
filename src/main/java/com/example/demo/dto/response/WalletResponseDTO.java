package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class WalletResponseDTO {
    private Long id;
    private String ownerType;
    private Long ownerId;
    private String currency;
    private BigDecimal balance;
    private OffsetDateTime createdAt;
}