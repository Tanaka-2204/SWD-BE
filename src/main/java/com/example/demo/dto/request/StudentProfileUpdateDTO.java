package com.example.demo.dto.request;

import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StudentProfileUpdateDTO {

    @Size(max = 200, message = "Full name must be less than 200 characters")
    private String fullName;

    @VietnamesePhoneNumber
    private String phoneNumber;

    private MultipartFile avatarFile;
}