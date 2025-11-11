package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PartnerResponseDTO {
    private UUID id;
    private String name;
    private String organizationType;
    private String contactEmail;
    private String contactPhone;
    private UUID walletId;
    private OffsetDateTime createdAt;
    private String status;
}