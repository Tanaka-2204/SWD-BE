package com.example.demo.repository;

import com.example.demo.entity.ProductInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInvoiceRepository extends JpaRepository<ProductInvoice, Long> {
}