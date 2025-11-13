package com.example.demo.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentProfileCompletionDTO {
    
    @NotBlank(message = "Phone number is required")
    @VietnamesePhoneNumber
    private String phoneNumber;
}