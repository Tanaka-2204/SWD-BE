package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class EventFundingResponseDTO {

    private Long id;

    private Long eventId;

    private Long partnerId;

    private String partnerName;

    private BigDecimal amountCoin;

    private OffsetDateTime createdAt;
}