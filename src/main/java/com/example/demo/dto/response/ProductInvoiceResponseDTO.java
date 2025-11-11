package com.example.demo.dto.response;

import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ProductInvoiceResponseDTO {
    private UUID invoiceId;
    private UUID studentId;
    private String studentName;
    private UUID productId;
    private String productTitle;
    private String productType;
    private Integer quantity;
    private BigDecimal totalCost;
    private String currency;
    private String status;
    private String verificationCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime deliveredAt;
    private String deliveredBy;
}