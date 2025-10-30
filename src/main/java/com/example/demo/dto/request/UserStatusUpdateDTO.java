package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserStatusUpdateDTO {

    @NotBlank(message = "Status is required.")
    @Pattern(
        regexp = "^(ACTIVE|INACTIVE)$", 
        message = "Status must be either 'ACTIVE' or 'INACTIVE'"
    )
    private String status;
}