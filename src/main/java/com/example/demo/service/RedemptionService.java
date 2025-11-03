package com.example.demo.service;

import com.example.demo.dto.response.WalletResponseDTO;

public interface RedemptionService {

    /**
     * Get the wallet information for the currently authenticated student (by Cognito sub).
     * @param cognitoSub Cognito subject from JWT
     * @return wallet response DTO with balance and metadata
     */
    WalletResponseDTO getStudentWalletByCognitoSub(String cognitoSub);
}
