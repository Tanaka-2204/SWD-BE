package com.example.demo.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class BroadcastRequestDTO {
    @NotNull
    private UUID eventId; // Sự kiện muốn gửi

    @NotBlank
    private String messageContent;
}