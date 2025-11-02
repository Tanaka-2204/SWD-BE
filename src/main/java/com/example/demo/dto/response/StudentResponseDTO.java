package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class StudentResponseDTO {
    
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private OffsetDateTime createdAt;
    private String status;
    private Long universityId;
    private String universityName;
    private Long walletId;
    private BigDecimal balance;
    private String currency;
}