package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestLoginResponseDTO {
     
    private String idToken;
    
    // JWT Access Token
    private String accessToken;
    
    // Refresh Token
    private String refreshToken;
}