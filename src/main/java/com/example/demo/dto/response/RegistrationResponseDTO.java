package com.example.demo.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RegistrationResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long eventId;
    private String eventTitle;
    private OffsetDateTime registeredAt;
}