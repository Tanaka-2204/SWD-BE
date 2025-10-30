package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentRequest {
    @NotNull(message = "University ID is required")
    private Long universityId;
    
    @NotNull(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;
    
    @NotNull(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;
    
    private String avatarUrl;
}
