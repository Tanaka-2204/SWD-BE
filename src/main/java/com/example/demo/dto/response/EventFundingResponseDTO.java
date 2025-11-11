package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EventFundingResponseDTO {

    private UUID id;

    private UUID eventId;

    private UUID partnerId;

    private String partnerName;

    private BigDecimal amountCoin;

    private OffsetDateTime createdAt;
}