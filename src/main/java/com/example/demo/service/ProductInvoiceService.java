package com.example.demo.service;

import com.example.demo.dto.request.ProductInvoiceRequestDTO;
import com.example.demo.dto.response.ProductInvoiceResponseDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductInvoiceService {

    ProductResponseDTO redeemProduct(ProductInvoiceRequestDTO request);

    ProductResponseDTO deliverInvoice(Long invoiceId, String deliveredBy);

    ProductResponseDTO cancelInvoice(Long invoiceId);

    ProductInvoiceResponseDTO getInvoiceById(Long invoiceId);

    Page<ProductInvoiceResponseDTO> getStudentInvoices(Long studentId, String status, String sortBy, String order, Integer limit, Integer offset);

    Map<String, Object> getInvoiceStats();
}