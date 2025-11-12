package com.example.demo.service;

import com.example.demo.dto.request.StockAdjustmentRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductAnalyticsService {
    ProductResponseDTO adjustStock(UUID productId, StockAdjustmentRequestDTO request);
    List<Map<String, Object>> getProductStatistics(int top);
    List<Product> getLowStockProducts(int threshold);
    Map<String, Object> getOverview();
}