package com.example.demo.service;

import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;

import java.math.BigDecimal;

public interface WalletService {

    WalletResponseDTO getWalletByOwner(String ownerType, Long ownerId);

    WalletTransactionResponseDTO adminTopupForPartner(WalletTopupRequestDTO topupRequest);

    void deductBalance(String ownerType, Long ownerId, BigDecimal amount, String referenceType, Long referenceId);

    void refundBalance(String ownerType, Long ownerId, BigDecimal amount, String referenceType, Long referenceId);
}