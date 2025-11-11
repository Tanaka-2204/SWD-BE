package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.WalletRedeemRequestDTO;
import com.example.demo.dto.request.WalletRollbackRequestDTO;
import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.request.WalletTransferRequestDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import java.util.UUID;
import java.math.BigDecimal;

public interface WalletService {

    // --- READ OPERATIONS ---
    WalletResponseDTO getWalletById(UUID walletId);

    Page<WalletTransactionResponseDTO> getTransactionHistory(UUID ownerId, String ownerType, Pageable pageable);

    WalletResponseDTO getWalletByOwner(String ownerType, UUID ownerId);

    Page<WalletTransactionResponseDTO> getTransactionHistoryForUser(AuthPrincipal principal, Pageable pageable);

    // --- WRITE OPERATIONS ---
    WalletTransactionResponseDTO adminTopupForPartner(WalletTopupRequestDTO topupRequest);

    WalletTransactionResponseDTO transferCoins(WalletTransferRequestDTO transferRequest);

    WalletTransactionResponseDTO redeemCoins(WalletRedeemRequestDTO redeemRequest);

    WalletTransactionResponseDTO rollbackTransaction(WalletRollbackRequestDTO rollbackRequest);

    void deductBalance(String ownerType, UUID ownerId, BigDecimal amount, String referenceType, UUID referenceId);

    void refundBalance(String ownerType, UUID ownerId, BigDecimal amount, String referenceType, UUID referenceId);

    Page<WalletTransactionResponseDTO> getAllTransactions(Pageable pageable);
}