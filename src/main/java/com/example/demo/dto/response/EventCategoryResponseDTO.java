package com.example.demo.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class EventCategoryResponseDTO {
    private UUID id;
    private String name;
    private String description;
}