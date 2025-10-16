package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequestDTO {
    @NotNull
    private Long studentId;
    @NotNull
    private Long eventId;
}