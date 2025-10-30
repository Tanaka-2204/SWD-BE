package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class StudentResponseDTO {
    private Long id;
    private Long universityId;
    private String universityName;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private OffsetDateTime createdAt;
    private String status;
}