package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    @NotBlank
    private String type; // GIFT | VOUCHER

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private BigDecimal unitCost;

    @NotNull
    private Integer totalStock;

    private String imageUrl;
}