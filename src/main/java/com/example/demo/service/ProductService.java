package com.example.demo.service;

import com.example.demo.dto.request.ProductRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import java.util.UUID;

import java.util.List;
import java.util.Map;

public interface ProductService {

    Page<ProductResponseDTO> getProducts(String category, Double minCost, Double maxCost, String sortBy, String order, Boolean isActive, Integer limit, Integer offset);

    ProductResponseDTO getProductById(UUID id);

    ProductResponseDTO createProduct(ProductRequestDTO request);

    ProductResponseDTO updateProduct(UUID id, ProductRequestDTO request);

    void deleteProduct(UUID id);

    List<Map<String, Object>> getTopProducts();

    List<ProductResponseDTO> getLowStockProducts();
}