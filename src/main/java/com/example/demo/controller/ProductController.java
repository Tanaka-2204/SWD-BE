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
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import com.example.demo.exception.BadRequestException;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "5. Store & Redemption")
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

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
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new product (Admin) - supports image upload")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductRequestDTO request;
        try {
            request = objectMapper.readValue(data, ProductRequestDTO.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON in 'data' part: " + e.getOriginalMessage());
        }

        Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BadRequestException(msg);
        }

        ProductResponseDTO product = productService.createProduct(request, image);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product (Admin)")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete product (Admin)")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
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