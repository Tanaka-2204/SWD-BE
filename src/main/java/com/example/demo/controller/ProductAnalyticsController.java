package com.example.demo.controller;

import com.example.demo.dto.request.StockAdjustmentRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "5. Store & Redemption")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ProductAnalyticsController {

    private final ProductAnalyticsService productAnalyticsService;

    @PatchMapping("/adjust-stock/{id}")
    @Operation(summary = "Adjust product stock manually")
    public ResponseEntity<ProductResponseDTO> adjustStock(@PathVariable UUID id,
                                                          @Valid @RequestBody StockAdjustmentRequestDTO request) {
        ProductResponseDTO dto = productAnalyticsService.adjustStock(id, request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get top products by revenue/redemptions")
    public ResponseEntity<List<Map<String, Object>>> statistics(@RequestParam(defaultValue = "10") Integer top) {
        List<Map<String, Object>> stats = productAnalyticsService.getProductStatistics(top);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products below stock threshold")
    public ResponseEntity<List<Product>> lowStock(@RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> items = productAnalyticsService.getLowStockProducts(threshold);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/overview")
    @Operation(summary = "Get analytics overview")
    public ResponseEntity<Map<String, Object>> overview() {
        Map<String, Object> m = productAnalyticsService.getOverview();
        return ResponseEntity.ok(m);
    }
}