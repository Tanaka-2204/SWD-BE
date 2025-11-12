package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequestDTO {
    @NotBlank
    private String adjustmentType;

    @NotNull
    @Min(1)
    private Integer amount;

    private String reason;
}