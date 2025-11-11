package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Data;
import java.util.UUID;

@Data
public class StudentResponseDTO {
    
    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private OffsetDateTime createdAt;
    private String status;
    private UUID universityId;
    private String universityName;
    private UUID walletId;
    private BigDecimal balance;
    private String currency;
}