package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private Long id;
    private Long universityId;
    private String universityName;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatarUrl;
    private OffsetDateTime createdAt;
}
