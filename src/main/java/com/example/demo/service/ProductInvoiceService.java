package com.example.demo.service;

import com.example.demo.dto.request.ProductInvoiceRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;

public interface ProductInvoiceService {

    ProductResponseDTO redeemProduct(ProductInvoiceRequestDTO request);

    ProductResponseDTO deliverInvoice(Long invoiceId, String deliveredBy);

    ProductResponseDTO cancelInvoice(Long invoiceId);
}