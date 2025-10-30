package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UniversityRequestDTO {

    @NotBlank(message = "University name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "University code is required (e.g., FPT, HCMUT)")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "University domain is required (e.g., fpt.edu.vn)")
    @Size(max = 100)
    private String domain;
}