package com.example.demo.service;

import com.example.demo.dto.request.WalletRedeemRequestDTO;
import com.example.demo.dto.request.WalletRollbackRequestDTO;
import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.request.WalletTransferRequestDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletService {

    // --- READ OPERATIONS ---
    WalletResponseDTO getWalletById(Long walletId);

    WalletResponseDTO getWalletByOwner(String ownerType, Long ownerId);

    Page<WalletTransactionResponseDTO> getWalletHistoryById(Long walletId, Pageable pageable);

    // --- WRITE OPERATIONS ---
    WalletTransactionResponseDTO adminTopupForPartner(WalletTopupRequestDTO topupRequest);

    WalletTransactionResponseDTO transferCoins(WalletTransferRequestDTO transferRequest);

    WalletTransactionResponseDTO redeemCoins(WalletRedeemRequestDTO redeemRequest);

    WalletTransactionResponseDTO rollbackTransaction(WalletRollbackRequestDTO rollbackRequest);
}