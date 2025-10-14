package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EventCategoryRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String name;
    private String description;
}