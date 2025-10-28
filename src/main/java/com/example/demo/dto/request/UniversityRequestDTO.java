package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UniversityRequestDTO {
    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 200)
    private String domain;
}