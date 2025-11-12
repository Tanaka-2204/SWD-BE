package com.example.demo.repository;

import com.example.demo.entity.ProductStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProductStockHistoryRepository extends JpaRepository<ProductStockHistory, UUID> {
}