package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "4. Wallet", description = "APIs for wallet and transaction management")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Get wallet by ID", description = "Retrieves wallet details using the wallet's unique ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved wallet"),
        @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponseDTO> getWalletById(
            @Parameter(description = "ID of the wallet") @PathVariable Long id) {
        // TODO: Add permission check (e.g., only owner or admin can view?)
        WalletResponseDTO wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Get wallet transaction history", description = "Retrieves a paginated list of transactions for a specific wallet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction history"),
        @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{id}/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDTO>> getWalletHistory(
            @Parameter(description = "ID of the wallet") @PathVariable Long id,
            Pageable pageable) {
        // TODO: Add permission check
        Page<WalletTransactionResponseDTO> history = walletService.getWalletHistoryById(id, pageable);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Admin top up coin for a partner", description = "Admin-only endpoint to add funds to a partner's wallet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Top-up successful"),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Partner or wallets not found")
    })
    @PostMapping("/admin/topup") // Sửa lại đường dẫn theo yêu cầu
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletTransactionResponseDTO> topupWalletForPartner(
            @Valid @RequestBody WalletTopupRequestDTO topupRequest) {
        WalletTransactionResponseDTO transaction = walletService.adminTopupForPartner(topupRequest);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Transfer coins between wallets", description = "Transfers a specified amount from one wallet to another. Requires appropriate permissions.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request (e.g., insufficient funds, same wallet)"),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not own the source wallet or lacks permission"),
        @ApiResponse(responseCode = "404", description = "Source or destination wallet not found")
    })
    @PostMapping("/transfer")
    // TODO: Add appropriate @PreAuthorize (e.g., check if user owns fromWalletId or is Admin)
    public ResponseEntity<WalletTransactionResponseDTO> transferCoins(
            @Valid @RequestBody WalletTransferRequestDTO transferRequest) {
        WalletTransactionResponseDTO transaction = walletService.transferCoins(transferRequest);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Redeem coins for product purchase", description = "Deducts coins from a student's wallet when they redeem a product. Typically called internally or by a secured endpoint.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Redemption successful"),
        @ApiResponse(responseCode = "400", description = "Insufficient funds"),
        @ApiResponse(responseCode = "404", description = "Student wallet not found")
    })
    @PostMapping("/redeem")
    // TODO: Secure this endpoint appropriately (internal call, specific role?)
    public ResponseEntity<WalletTransactionResponseDTO> redeemCoins(
            @Valid @RequestBody WalletRedeemRequestDTO redeemRequest) {
        WalletTransactionResponseDTO transaction = walletService.redeemCoins(redeemRequest);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Rollback a transaction", description = "Reverses a previously completed transaction (e.g., for refunds). Requires appropriate permissions.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rollback successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request (e.g., transaction already rolled back)"),
        @ApiResponse(responseCode = "403", description = "Forbidden: User lacks permission"),
        @ApiResponse(responseCode = "404", description = "Original transaction or wallets not found")
    })
    @PostMapping("/rollback")
    // TODO: Add appropriate @PreAuthorize (e.g., Admin or Partner role depending on context)
    public ResponseEntity<WalletTransactionResponseDTO> rollbackTransaction(
            @Valid @RequestBody WalletRollbackRequestDTO rollbackRequest) {
        WalletTransactionResponseDTO transaction = walletService.rollbackTransaction(rollbackRequest);
        return ResponseEntity.ok(transaction);
    }
}