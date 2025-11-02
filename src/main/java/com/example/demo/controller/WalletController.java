package com.example.demo.controller;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "4. Wallet & Transactions")
@SecurityRequirement(name = "bearerAuth")
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
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<WalletResponseDTO> getWalletById(
            @Parameter(description = "ID of the wallet") @PathVariable Long id) {
        WalletResponseDTO wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Get current user's transaction history", description = "Retrieves a paginated list of transactions for the authenticated user's wallet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction history"),
        @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/me/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponseDTO<WalletTransactionResponseDTO>> getMyTransactionHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionTime,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        
        // Logic tìm walletId/ownerType từ principal (Service nên xử lý việc này)
        Page<WalletTransactionResponseDTO> history = walletService.getTransactionHistoryForUser(principal, pageable);
        
        return ResponseEntity.ok(new PageResponseDTO<>(history));
    }

    @Operation(summary = "Transfer coins between wallets", description = "Transfers a specified amount from one wallet to another. Requires appropriate permissions.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request (e.g., insufficient funds, same wallet)"),
        @ApiResponse(responseCode = "403", description = "Forbidden: User does not own the source wallet or lacks permission"),
        @ApiResponse(responseCode = "404", description = "Source or destination wallet not found")
    })
    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("ADMIN")
    public ResponseEntity<WalletTransactionResponseDTO> rollbackTransaction(
            @Valid @RequestBody WalletRollbackRequestDTO rollbackRequest) {
        WalletTransactionResponseDTO transaction = walletService.rollbackTransaction(rollbackRequest);
        return ResponseEntity.ok(transaction);
    }

    private Pageable createPageable(int page, int size, String sort) {
        int pageIndex = page > 0 ? page - 1 : 0;
        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) 
                                        ? Sort.Direction.DESC 
                                        : Sort.Direction.ASC;
            return PageRequest.of(pageIndex, size, Sort.by(direction, sortField));
        } catch (Exception e) {
            return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "transactionTime"));
        }
    }
}