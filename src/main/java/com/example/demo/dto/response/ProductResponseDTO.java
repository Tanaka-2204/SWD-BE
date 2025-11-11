package com.example.demo.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ProductResponseDTO {
    private UUID id;
    private String type;
    private String title;
    private String description;
    private BigDecimal unitCost;
    private String currency;
    private Integer totalStock;
    private String imageUrl;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}