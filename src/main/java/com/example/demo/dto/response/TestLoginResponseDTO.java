package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestLoginResponseDTO {
    private String accessToken;
    private String idToken; // Thêm cả IdToken nếu bạn cần
}