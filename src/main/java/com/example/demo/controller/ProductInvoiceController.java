package com.example.demo.controller;

import com.example.demo.dto.request.ProductInvoiceRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.service.ProductInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Product Invoice", description = "Product invoice management APIs")
public class ProductInvoiceController {

    private final ProductInvoiceService productInvoiceService;

    @PostMapping
    @Operation(summary = "Redeem product - create invoice and deduct balance")
    public ResponseEntity<ProductResponseDTO> redeemProduct(@Valid @RequestBody ProductInvoiceRequestDTO request) {
        ProductResponseDTO product = productInvoiceService.redeemProduct(request);
        return ResponseEntity.ok(product);
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
}