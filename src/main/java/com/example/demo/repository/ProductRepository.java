package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.type = :category) AND " +
           "(:minCost IS NULL OR p.unitCost >= :minCost) AND " +
           "(:maxCost IS NULL OR p.unitCost <= :maxCost) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> findProductsWithFilters(@Param("category") String category,
                                         @Param("minCost") BigDecimal minCost,
                                         @Param("maxCost") BigDecimal maxCost,
                                         @Param("isActive") Boolean isActive,
                                         Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.totalStock ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.totalStock < :threshold ORDER BY p.totalStock ASC")
    List<Product> findLowStockProductsBelow(@Param("threshold") Integer threshold);
}