package com.example.demo.dto.request;

import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinRequestDTO {
    @NotNull
    private Long eventId;

    @NotBlank
    @VietnamesePhoneNumber
    private String phoneNumber;
}