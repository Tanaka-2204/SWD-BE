package com.example.demo.dto.response;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AdminResponseDTO {
    
    private UUID id;
    private String email;
    private String fullName;
    private OffsetDateTime createdAt;
    private String cognitoSub;
}
