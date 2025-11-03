package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
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

@RestController
@RequestMapping("/api/v1/redemptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RedemptionController {

    private static final Logger log = LoggerFactory.getLogger(RedemptionController.class);
    private final RedemptionService redemptionService;

    @GetMapping("/wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletResponseDTO> getMyWallet(@AuthenticationPrincipal AuthPrincipal principal) {
        log.info("Get wallet for cognitoSub={}", principal.getCognitoSub());
        WalletResponseDTO wallet = redemptionService.getStudentWalletByCognitoSub(principal.getCognitoSub());
        return ResponseEntity.ok(wallet);
    }
}