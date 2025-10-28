package com.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequestDTO {
    
    @NotNull
    @Min(1)
    @Max(5)
    private Short rating; // Ví dụ: 1-5 sao
    
    private String comments;
}