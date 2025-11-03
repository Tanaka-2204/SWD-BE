package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ErrorResponseDTO {
    private int statusCode;
    private String message;
    private OffsetDateTime timestamp;

    public ErrorResponseDTO(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = OffsetDateTime.now();
    }
}