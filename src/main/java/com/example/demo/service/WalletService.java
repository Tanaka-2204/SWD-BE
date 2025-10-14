package com.example.demo.service;

import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;

public interface WalletService {

    WalletResponseDTO getWalletByOwner(String ownerType, Long ownerId);

    WalletTransactionResponseDTO adminTopupForPartner(WalletTopupRequestDTO topupRequest);
}