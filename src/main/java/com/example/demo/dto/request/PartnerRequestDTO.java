package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PartnerRequestDTO {
    
    @NotBlank // THÊM USERNAME (không phải email)
    @Size(min = 3, max = 100) // Đặt giới hạn phù hợp
    private String username;
    
    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String organizationType; // longterm | shortterm 

    private String contactEmail;
    
    private String contactPhone;
}