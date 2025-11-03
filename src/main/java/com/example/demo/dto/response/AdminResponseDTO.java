package com.example.demo.dto.response;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class AdminResponseDTO {
    
    private Long id;
    private String email;
    private String fullName;
    private OffsetDateTime createdAt;
    private String cognitoSub;
}
