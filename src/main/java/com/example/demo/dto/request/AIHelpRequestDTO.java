package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AIHelpRequestDTO {
    @NotBlank
    private String question;

    @NotNull
    private UUID userId;
}