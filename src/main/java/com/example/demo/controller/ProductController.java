package com.example.demo.controller;

import com.example.demo.dto.request.ProductRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "3. Store & Redemption")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products list with sort/filter")
    public ResponseEntity<Map<String, Object>> getProducts(
            @Parameter(description = "Filter by category (GIFT, VOUCHER)") @RequestParam(required = false) String category,
            @Parameter(description = "Min cost filter") @RequestParam(required = false) Double minCost,
            @Parameter(description = "Max cost filter") @RequestParam(required = false) Double maxCost,
            @Parameter(description = "Sort by (popularity, cost, createdAt, stock)") @RequestParam(defaultValue = "popularity") String sortBy,
            @Parameter(description = "Sort order (asc, desc)") @RequestParam(defaultValue = "desc") String order,
            @Parameter(description = "Only active products") @RequestParam(defaultValue = "true") Boolean isActive,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset) {
        Page<ProductResponseDTO> page = productService.getProducts(category, minCost, maxCost, sortBy, order, isActive, limit, offset);
        Map<String, Object> response = Map.of(
                "data", page.getContent(),
                "metadata", Map.of(
                        "totalItems", page.getTotalElements(),
                        "page", page.getNumber(),
                        "pageSize", page.getSize()
                )
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @Operation(summary = "Create new product (Admin)")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product (Admin)")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete product (Admin)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/top")
    @Operation(summary = "Get top redeemed products")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts() {
        List<Map<String, Object>> topProducts = productService.getTopProducts();
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products")
    public ResponseEntity<List<ProductResponseDTO>> getLowStockProducts() {
        List<ProductResponseDTO> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }
}