package com.example.demo.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BroadcastRequestDTO {
    @NotNull
    private Long eventId; // Sự kiện muốn gửi

    @NotBlank
    private String messageContent;
}