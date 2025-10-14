package com.example.demo.controller;

import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.demo.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Get wallet by owner", description = "Retrieves wallet details for a specific owner (e.g., PARTNER, STUDENT).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet"),
            @ApiResponse(responseCode = "404", description = "Wallet not found for the specified owner")
    })
    @GetMapping("/{ownerType}/{ownerId}")
    public ResponseEntity<WalletResponseDTO> getWalletByOwner(
            @Parameter(description = "Type of the owner (e.g., PARTNER, STUDENT)") @PathVariable String ownerType,
            @Parameter(description = "ID of the owner") @PathVariable Long ownerId) {
        WalletResponseDTO wallet = walletService.getWalletByOwner(ownerType, ownerId);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Admin top up coin for a partner", description = "An admin-only endpoint to add funds to a partner's wallet.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top-up successful, transaction details returned"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., negative amount)"),
            @ApiResponse(responseCode = "404", description = "Partner or their wallet not found")
    })
    @PostMapping("/topup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletTransactionResponseDTO> topupWalletForPartner(
            @Valid @RequestBody WalletTopupRequestDTO topupRequest) {
        WalletTransactionResponseDTO transaction = walletService.adminTopupForPartner(topupRequest);
        return ResponseEntity.ok(transaction);
    }
}