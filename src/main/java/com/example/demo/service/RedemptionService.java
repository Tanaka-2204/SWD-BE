package com.example.demo.service;

import com.example.demo.dto.response.ProductInvoiceResponseDTO;
import com.example.demo.dto.response.WalletResponseDTO;

import java.util.List;

public interface RedemptionService {
    
    /**
     * Get the wallet information for the currently authenticated student (by Cognito sub).
     * @param cognitoSub Cognito subject from JWT
     * @return wallet response DTO with balance and metadata
     */
    WalletResponseDTO getStudentWalletByCognitoSub(String cognitoSub);

    /**
     * Redeem a product: deduct coins, reduce stock, create invoice
     * @param cognitoSub Cognito subject from JWT
     * @param productId ID of the product to redeem
     * @return ProductInvoiceResponseDTO with invoice details
     */
    ProductInvoiceResponseDTO redeemProduct(String cognitoSub, Long productId);

    /**
     * Get the list of invoices for redeemed products by the student
     * @param cognitoSub Cognito subject from JWT
     * @return List of ProductInvoiceResponseDTO with invoice details
     */
    List<ProductInvoiceResponseDTO> getStudentInvoices(String cognitoSub);
}
