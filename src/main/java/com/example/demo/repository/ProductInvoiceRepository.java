package com.example.demo.repository;

import com.example.demo.entity.ProductInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductInvoiceRepository extends JpaRepository<ProductInvoice, Long> {

    @Query("SELECT pi FROM ProductInvoice pi WHERE pi.student.id = :studentId " +
           "AND (:status IS NULL OR pi.status = :status) " +
           "ORDER BY CASE WHEN :sortBy = 'createdAt' THEN pi.createdAt END DESC, " +
           "CASE WHEN :sortBy = 'totalCost' THEN pi.totalCost END DESC")
    Page<ProductInvoice> findInvoicesByStudent(@Param("studentId") Long studentId,
                                              @Param("status") String status,
                                              @Param("sortBy") String sortBy,
                                              Pageable pageable);

    @Query("SELECT new map(COUNT(pi.id) as totalRedeems, SUM(pi.totalCost) as totalCoinsSpent, " +
           "new map(p.title as title, COUNT(pi.id) as count) as topProducts) " +
           "FROM ProductInvoice pi JOIN pi.product p " +
           "WHERE pi.status = 'DELIVERED'")
    List<Object[]> getInvoiceStats();
}