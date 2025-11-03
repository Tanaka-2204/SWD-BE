package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.response.ProductInvoiceResponseDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.service.RedemptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/redemptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RedemptionController {

    private static final Logger log = LoggerFactory.getLogger(RedemptionController.class);
    private final RedemptionService redemptionService;

    /**
     * API 1: Kiểm tra ví của sinh viên đang đăng nhập
     * GET /api/v1/redemptions/wallet
     */
    @GetMapping("/wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletResponseDTO> getMyWallet(@AuthenticationPrincipal AuthPrincipal principal) {
        log.info("Get wallet for cognitoSub={}", principal.getCognitoSub());
        WalletResponseDTO wallet = redemptionService.getStudentWalletByCognitoSub(principal.getCognitoSub());
        return ResponseEntity.ok(wallet);
    }

    /**
     * API 2: Đổi quà (redeem product)
     * POST /api/v1/products/{productId}/redeem
     */
    @PostMapping("/products/{productId}/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductInvoiceResponseDTO> redeemProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        log.info("Student {} redeeming product {}", principal.getCognitoSub(), productId);
        ProductInvoiceResponseDTO invoice = redemptionService.redeemProduct(principal.getCognitoSub(), productId);
        return ResponseEntity.ok(invoice);
    }

    /**
     * API 3: Lấy danh sách hóa đơn đổi quà của sinh viên
     * GET /api/v1/redemptions/invoices
     */
    @GetMapping("/invoices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductInvoiceResponseDTO>> getMyInvoices(@AuthenticationPrincipal AuthPrincipal principal) {
        log.info("Get invoices for student {}", principal.getCognitoSub());
        List<ProductInvoiceResponseDTO> invoices = redemptionService.getStudentInvoices(principal.getCognitoSub());
        return ResponseEntity.ok(invoices);
    }
}