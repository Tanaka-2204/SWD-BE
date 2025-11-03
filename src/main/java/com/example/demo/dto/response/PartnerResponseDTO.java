package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PartnerResponseDTO {
    private Long id;
    private String name;
    private String organizationType;
    private String contactEmail;
    private String contactPhone;
    private Long walletId;
    private OffsetDateTime createdAt;
    private String status;
}