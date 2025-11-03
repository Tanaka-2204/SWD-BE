package com.example.demo.dto.request;

import jakarta.validation.constraints.Email; // <<< THÊM IMPORT
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestLoginRequestDTO {
    
    @NotBlank
    @Email(message = "Email format is invalid") // <<< THÊM VALIDATION
    private String email; // <<< SỬA TỪ "username"
    
    @NotBlank
    private String password;
}