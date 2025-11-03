package com.example.demo.controller;

import com.example.demo.dto.request.ProductInvoiceRequestDTO;
import com.example.demo.dto.response.ProductInvoiceResponseDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.service.ProductInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "3. Store & Redemption")
@SecurityRequirement(name = "bearerAuth")
public class ProductInvoiceController {

    private final ProductInvoiceService productInvoiceService;

    @PostMapping
    @Operation(summary = "Redeem product - create invoice and deduct balance")
    public ResponseEntity<ProductResponseDTO> redeemProduct(@Valid @RequestBody ProductInvoiceRequestDTO request) {
        ProductResponseDTO product = productInvoiceService.redeemProduct(request);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice detail")
    public ResponseEntity<ProductInvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        ProductInvoiceResponseDTO invoice = productInvoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "Get student redeem history")
    public ResponseEntity<Map<String, Object>> getStudentInvoices(
            @PathVariable Long studentId,
            @Parameter(description = "Filter by status (PENDING, DELIVERED, CANCELLED)") @RequestParam(required = false) String status,
            @Parameter(description = "Sort by (createdAt, totalCost)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order (asc, desc)") @RequestParam(defaultValue = "desc") String order,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset) {
        Page<ProductInvoiceResponseDTO> page = productInvoiceService.getStudentInvoices(studentId, status, sortBy, order, limit, offset);
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

    @PutMapping("/{id}/deliver")
    @Operation(summary = "Deliver invoice - mark as delivered")
    public ResponseEntity<ProductResponseDTO> deliverInvoice(@PathVariable Long id, @RequestParam String deliveredBy) {
        ProductResponseDTO product = productInvoiceService.deliverInvoice(id, deliveredBy);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel invoice - refund balance and restore stock")
    public ResponseEntity<ProductResponseDTO> cancelInvoice(@PathVariable Long id) {
        ProductResponseDTO product = productInvoiceService.cancelInvoice(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get redeem statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStats() {
        Map<String, Object> stats = productInvoiceService.getInvoiceStats();
        return ResponseEntity.ok(stats);
    }
}