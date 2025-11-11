package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UniversityResponseDTO {
    private UUID id;
    private String name;
    private String code;
    private String domain;
    private OffsetDateTime createdAt;
}