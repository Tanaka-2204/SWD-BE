package com.example.demo.service.impl;

import com.example.demo.dto.request.ProductRequestDTO;
import com.example.demo.dto.response.ProductResponseDTO;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProducts(String category, Double minCost, Double maxCost, String sortBy, String order, Boolean isActive, Integer limit, Integer offset) {
        Sort sort = createSort(sortBy, order);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);

        BigDecimal minCostBD = minCost != null ? BigDecimal.valueOf(minCost) : null;
        BigDecimal maxCostBD = maxCost != null ? BigDecimal.valueOf(maxCost) : null;

        Page<Product> products = productRepository.findProductsWithFilters(category, minCostBD, maxCostBD, isActive, pageable);
        return products.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        Product product = new Product();
        product.setType(request.getType());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setUnitCost(request.getUnitCost());
        product.setCurrency("COIN");
        product.setTotalStock(request.getTotalStock());
        product.setImageUrl(request.getImageUrl());
        product.setIsActive(true);
        product.setCreatedAt(OffsetDateTime.now());

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setType(request.getType());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setUnitCost(request.getUnitCost());
        product.setTotalStock(request.getTotalStock());
        product.setImageUrl(request.getImageUrl());

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = productRepository.findTopProducts(pageable);
        return results.stream()
                .map(row -> Map.of(
                        "productId", row[0],
                        "title", row[1],
                        "redeemCount", row[2]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Sort createSort(String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        switch (sortBy.toLowerCase()) {
            case "popularity":
                // For now, sort by createdAt as popularity proxy
                return Sort.by(direction, "createdAt");
            case "cost":
                return Sort.by(direction, "unitCost");
            case "stock":
                return Sort.by(direction, "totalStock");
            case "createdat":
            default:
                return Sort.by(direction, "createdAt");
        }
    }

    private ProductResponseDTO convertToDTO(Product product) {
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