package com.example.demo.dto.request;

import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentCreateDTO {
    @NotNull
    private Long universityId;

    @NotBlank
    @Size(max = 200)
    private String fullName;

    @NotBlank
    @VietnamesePhoneNumber
    private String phoneNumber;

    private String email;
    private String avatarUrl;
}