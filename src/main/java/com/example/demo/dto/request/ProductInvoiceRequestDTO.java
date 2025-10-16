package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProductInvoiceRequestDTO {
    @NotNull
    private Long studentId;

    @NotNull
    private Long productId;

    @Min(1)
    private int quantity = 1;
}