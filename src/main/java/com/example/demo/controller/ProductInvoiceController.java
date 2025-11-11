package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
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
    public ResponseEntity<ProductInvoiceResponseDTO> getInvoiceById(@PathVariable UUID id) {
        ProductInvoiceResponseDTO invoice = productInvoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "Get student redeem history")
    public ResponseEntity<Map<String, Object>> getStudentInvoices(
            @PathVariable UUID studentId,
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

    @PostMapping("/{id}/confirm-delivery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARTNERS')")
    @Operation(summary = "Confirm delivery - Change invoice status from PENDING to DELIVERED (ADMIN/PARTNERS only)")
    public ResponseEntity<ProductInvoiceResponseDTO> confirmDelivery(
            @Parameter(description = "ID of the invoice to confirm delivery") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        
        // Lấy thông tin người xác nhận từ principal
        String deliveredBy = principal.getUsername(); // hoặc principal.getEmail()
        
        // Gọi service để cập nhật status (cập nhật product stock nếu cần)
        productInvoiceService.deliverInvoice(id, deliveredBy);
        
        // Lấy lại thông tin invoice đã cập nhật để trả về đầy đủ
        ProductInvoiceResponseDTO invoiceResponse = productInvoiceService.getInvoiceById(id);
        
        return ResponseEntity.ok(invoiceResponse);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel invoice - Refund balance and restore stock (All authenticated users)")
    public ResponseEntity<ProductInvoiceResponseDTO> cancelInvoice(
            @Parameter(description = "ID of the invoice to cancel") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        
        // Gọi service để hủy đơn hàng (hoàn tiền và restore stock)
        productInvoiceService.cancelInvoice(id);
        
        // Lấy lại thông tin invoice đã cập nhật để trả về đầy đủ
        ProductInvoiceResponseDTO invoiceResponse = productInvoiceService.getInvoiceById(id);
        
        return ResponseEntity.ok(invoiceResponse);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get redeem statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStats() {
        Map<String, Object> stats = productInvoiceService.getInvoiceStats();
        return ResponseEntity.ok(stats);
    }
}