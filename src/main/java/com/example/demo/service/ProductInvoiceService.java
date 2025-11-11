package com.example.demo.service;

import com.example.demo.dto.request.ProductInvoiceRequestDTO;
import com.example.demo.dto.response.ProductInvoiceResponseDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import java.util.UUID;
import java.util.Map;

public interface ProductInvoiceService {

    ProductResponseDTO redeemProduct(ProductInvoiceRequestDTO request);

    ProductResponseDTO deliverInvoice(UUID invoiceId, String deliveredBy);

    ProductResponseDTO cancelInvoice(UUID invoiceId);

    ProductInvoiceResponseDTO getInvoiceById(UUID invoiceId);

    Page<ProductInvoiceResponseDTO> getStudentInvoices(UUID studentId, String status, String sortBy, String order, Integer limit, Integer offset);

    Map<String, Object> getInvoiceStats();
}