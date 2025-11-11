package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.UUID;

@Data
public class ProductInvoiceRequestDTO {
    @NotNull
    private UUID studentId;

    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity = 1;
}