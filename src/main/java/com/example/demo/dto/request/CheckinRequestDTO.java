package com.example.demo.dto.request;

import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckinRequestDTO {

    @NotBlank
    @VietnamesePhoneNumber
    private String phoneNumber;
}