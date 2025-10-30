package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestLoginRequestDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}