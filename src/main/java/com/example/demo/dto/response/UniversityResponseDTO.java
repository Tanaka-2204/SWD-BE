package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class UniversityResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String domain;
    private OffsetDateTime createdAt;
}