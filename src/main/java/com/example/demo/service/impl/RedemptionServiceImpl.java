package com.example.demo.service.impl;

import com.example.demo.dto.response.ProductInvoiceResponseDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID; // <<< THÊM IMPORT
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedemptionServiceImpl implements RedemptionService {

    private static final Logger log = LoggerFactory.getLogger(RedemptionServiceImpl.class);

    private final StudentRepository studentRepository;
    private final ProductRepository productRepository;
    private final ProductInvoiceRepository productInvoiceRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getStudentWalletByCognitoSub(String cognitoSub) {
        Student student = studentRepository.findByCognitoSubWithWallet(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Wallet wallet = student.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for student");
        }

        log.info("Wallet for student {} -> balance={}", student.getId(), wallet.getBalance());

        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(wallet.getId());
        dto.setOwnerType(wallet.getOwnerType());
        dto.setOwnerId(wallet.getOwnerId());
        dto.setCurrency(wallet.getCurrency());
        dto.setBalance(wallet.getBalance());
        dto.setCreatedAt(wallet.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional
    public ProductInvoiceResponseDTO redeemProduct(String cognitoSub, UUID productId) { // SỬA: Long -> UUID
        // 1. Tìm student với wallet
        Student student = studentRepository.findByCognitoSubWithWallet(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // 2. Tìm product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // 3. Kiểm tra product active và còn stock
        if (product.getIsActive() == null || !product.getIsActive()) {
            throw new BadRequestException("Product is not active");
        }

        if (product.getTotalStock() <= 0) {
            throw new BadRequestException("Product is out of stock");
        }

        // 4. Lấy ví của student
        Wallet wallet = student.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for student");
        }

        // 5. Kiểm tra balance đủ không (1 sản phẩm)
        BigDecimal productCost = product.getUnitCost();
        if (wallet.getBalance().compareTo(productCost) < 0) {
            throw new BadRequestException("Insufficient balance. Required: " + productCost + ", Available: " + wallet.getBalance());
        }

        // 6. Trừ coin từ ví
        wallet.setBalance(wallet.getBalance().subtract(productCost));
        // Tăng version để tránh race condition (nếu dùng optimistic locking)
        if (wallet.getVersion() != null) {
             wallet.setVersion(wallet.getVersion() + 1);
        }
        walletRepository.save(wallet);

        // 7. Giảm stock của product
        product.setTotalStock(product.getTotalStock() - 1);
        productRepository.save(product);

        // 9. Tạo product invoice (Để có ID trước khi tạo WalletTransaction)
        ProductInvoice invoice = new ProductInvoice();
        invoice.setStudent(student);
        invoice.setProduct(product);
        invoice.setQuantity(1); // Mỗi lần đổi 1 sản phẩm
        invoice.setTotalCost(productCost);
        invoice.setCurrency(product.getCurrency());
        invoice.setStatus("PENDING");
        invoice.setVerificationCode(generateVerificationCode());
        invoice.setCreatedAt(OffsetDateTime.now());
        ProductInvoice savedInvoice = productInvoiceRepository.save(invoice);


        // 8. Tạo wallet transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(productCost.negate()); // Số âm vì trừ tiền
        transaction.setTxnType("PRODUCT_REDEEM");
        transaction.setReferenceType("PRODUCT_INVOICE");
        transaction.setReferenceId(savedInvoice.getId()); // Gán ID của Invoice vừa tạo
        transaction.setCreatedAt(OffsetDateTime.now());
        walletTransactionRepository.save(transaction);


        log.info("Student {} redeemed product {} for {} coins. New balance: {}", 
                 student.getId(), productId, productCost, wallet.getBalance());

        return convertToProductInvoiceResponseDTO(savedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductInvoiceResponseDTO> getStudentInvoices(String cognitoSub) {
        Student student = studentRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Dùng method có sẵn trong ProductInvoiceRepository
        List<ProductInvoice> invoices = productInvoiceRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());

        return invoices.stream()
                .map(this::convertToProductInvoiceResponseDTO)
                .collect(Collectors.toList());
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private ProductInvoiceResponseDTO convertToProductInvoiceResponseDTO(ProductInvoice invoice) {
        ProductInvoiceResponseDTO dto = new ProductInvoiceResponseDTO();
        dto.setInvoiceId(invoice.getId());
        dto.setStudentId(invoice.getStudent().getId());
        dto.setStudentName(invoice.getStudent().getFullName());
        dto.setProductId(invoice.getProduct().getId());
        dto.setProductTitle(invoice.getProduct().getTitle());
        dto.setProductType(invoice.getProduct().getType());
        dto.setQuantity(invoice.getQuantity());
        dto.setTotalCost(invoice.getTotalCost());
        dto.setCurrency(invoice.getCurrency());
        dto.setStatus(invoice.getStatus());
        dto.setVerificationCode(invoice.getVerificationCode());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setDeliveredAt(invoice.getDeliveredAt());
        dto.setDeliveredBy(invoice.getDeliveredBy());
        return dto;
    }
}