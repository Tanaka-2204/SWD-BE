package com.example.demo.service.impl;

import com.example.demo.dto.request.StockAdjustmentRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductStockHistory;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.ProductInvoiceRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductStockHistoryRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.ProductAnalyticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductAnalyticsServiceImpl implements ProductAnalyticsService {

    private final ProductRepository productRepository;
    private final ProductStockHistoryRepository productStockHistoryRepository;
    private final ProductInvoiceRepository productInvoiceRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;

    public ProductAnalyticsServiceImpl(ProductRepository productRepository,
                                       ProductStockHistoryRepository productStockHistoryRepository,
                                       ProductInvoiceRepository productInvoiceRepository,
                                       StudentRepository studentRepository,
                                       EventRepository eventRepository) {
        this.productRepository = productRepository;
        this.productStockHistoryRepository = productStockHistoryRepository;
        this.productInvoiceRepository = productInvoiceRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public ProductResponseDTO adjustStock(UUID productId, StockAdjustmentRequestDTO request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        String type = request.getAdjustmentType();
        Integer amount = request.getAmount();
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        if ("DECREASE".equalsIgnoreCase(type)) {
            if (product.getTotalStock() < amount) {
                throw new BadRequestException("Insufficient stock to decrease");
            }
            product.setTotalStock(product.getTotalStock() - amount);
        } else if ("INCREASE".equalsIgnoreCase(type)) {
            product.setTotalStock(product.getTotalStock() + amount);
        } else {
            throw new BadRequestException("Invalid adjustmentType");
        }

        ProductStockHistory history = new ProductStockHistory();
        history.setProduct(product);
        history.setAmountChanged(amount);
        history.setType(type.toUpperCase());
        history.setReason(request.getReason());
        history.setCreatedAt(OffsetDateTime.now());

        productRepository.save(product);
        productStockHistoryRepository.save(history);

        return toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductStatistics(int top) {
        Pageable pageable = PageRequest.of(0, top);
        List<Object[]> rows = productInvoiceRepository.findDeliveredStats(pageable);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", row[0]);
            m.put("title", row[1]);
            m.put("totalRedeem", row[2]);
            m.put("totalCoins", row[3]);
            list.add(m);
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProductsBelow(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOverview() {
        long totalProducts = productRepository.count();
        long totalRedeemed = productInvoiceRepository.countByStatus("DELIVERED");
        BigDecimal totalCoinsUsed = productInvoiceRepository.sumDeliveredCoins();
        long studentCount = studentRepository.count();
        double averageRedeemPerStudent = studentCount == 0 ? 0.0 : (double) totalRedeemed / (double) studentCount;

        List<Object[]> partnerRows = eventRepository.findPartnerActivity();
        String mostActivePartner = partnerRows.isEmpty() ? null : String.valueOf(partnerRows.get(0)[0]);

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalProducts", totalProducts);
        overview.put("totalRedeemed", totalRedeemed);
        overview.put("totalCoinsUsed", totalCoinsUsed);
        overview.put("averageRedeemPerStudent", averageRedeemPerStudent);
        overview.put("mostActivePartner", mostActivePartner);
        return overview;
    }

    private ProductResponseDTO toDto(Product product) {
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