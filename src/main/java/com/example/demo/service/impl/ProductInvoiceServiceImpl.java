package com.example.demo.service.impl;

import com.example.demo.dto.request.ProductInvoiceRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductInvoice;
import com.example.demo.entity.Student;
import com.example.demo.entity.Wallet;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductInvoiceRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.ProductInvoiceService;
import com.example.demo.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ProductInvoiceServiceImpl implements ProductInvoiceService {

    private final ProductInvoiceRepository productInvoiceRepository;
    private final ProductRepository productRepository;
    private final StudentRepository studentRepository;
    private final WalletService walletService;

    public ProductInvoiceServiceImpl(ProductInvoiceRepository productInvoiceRepository,
                                     ProductRepository productRepository,
                                     StudentRepository studentRepository,
                                     WalletService walletService) {
        this.productInvoiceRepository = productInvoiceRepository;
        this.productRepository = productRepository;
        this.studentRepository = studentRepository;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public ProductResponseDTO redeemProduct(ProductInvoiceRequestDTO request) {
        // 1. Validate student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        // 2. Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getIsActive() == null || !product.getIsActive()) {
            throw new IllegalStateException("Product is not active");
        }

        if (product.getTotalStock() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock");
        }

        // 3. Calculate total cost
        BigDecimal totalCost = product.getUnitCost().multiply(BigDecimal.valueOf(request.getQuantity()));

        // 4. Check wallet balance - will be handled by walletService.deductBalance

        // 6. Update stock
        product.setTotalStock(product.getTotalStock() - request.getQuantity());

        // 7. Create invoice
        ProductInvoice invoice = new ProductInvoice();
        invoice.setStudent(student);
        invoice.setProduct(product);
        invoice.setQuantity(request.getQuantity());
        invoice.setTotalCost(totalCost);
        invoice.setCurrency(product.getCurrency());
        invoice.setStatus("PENDING");
        invoice.setVerificationCode(generateVerificationCode());

        ProductInvoice savedInvoice = productInvoiceRepository.save(invoice);

        // 8. Create wallet transaction
        walletService.deductBalance("STUDENT", student.getId(), totalCost, "PRODUCT_INVOICE", savedInvoice.getId());

        return convertProductToDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO deliverInvoice(Long invoiceId, String deliveredBy) {
        ProductInvoice invoice = productInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (!"PENDING".equals(invoice.getStatus())) {
            throw new IllegalStateException("Invoice is not in PENDING status");
        }

        invoice.setStatus("DELIVERED");
        invoice.setDeliveredAt(OffsetDateTime.now());
        invoice.setDeliveredBy(deliveredBy);

        productInvoiceRepository.save(invoice);

        return convertProductToDTO(invoice.getProduct());
    }

    @Override
    @Transactional
    public ProductResponseDTO cancelInvoice(Long invoiceId) {
        ProductInvoice invoice = productInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (!"PENDING".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only PENDING invoices can be cancelled");
        }

        // Refund balance
        walletService.refundBalance("STUDENT", invoice.getStudent().getId(), invoice.getTotalCost(), "PRODUCT_INVOICE", invoice.getId());

        // Restore stock
        Product product = invoice.getProduct();
        product.setTotalStock(product.getTotalStock() + invoice.getQuantity());

        invoice.setStatus("CANCELLED");

        productInvoiceRepository.save(invoice);

        return convertProductToDTO(product);
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private ProductResponseDTO convertProductToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setType(product.getType());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setUnitCost(product.getUnitCost());
        dto.setCurrency(product.getCurrency());
        dto.setTotalStock(product.getTotalStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}