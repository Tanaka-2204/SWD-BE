package com.example.demo.repository;

import com.example.demo.entity.ProductInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface ProductInvoiceRepository extends JpaRepository<ProductInvoice, UUID> {
    
    List<ProductInvoice> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    @Query("SELECT pi FROM ProductInvoice pi WHERE pi.student.id = :studentId " +
           "AND (:status IS NULL OR pi.status = :status)")
    Page<ProductInvoice> findInvoicesByStudent(@Param("studentId") UUID studentId, 
                                                @Param("status") String status, 
                                                Pageable pageable);

    @Query("SELECT COUNT(pi), COALESCE(SUM(pi.totalCost), 0) FROM ProductInvoice pi")
    List<Object[]> getInvoiceStats();

    @Query("SELECT p.id, p.title, COUNT(pi.id) AS totalRedeem, COALESCE(SUM(pi.totalCost),0) AS totalCoins " +
           "FROM ProductInvoice pi JOIN pi.product p " +
           "WHERE pi.status = 'DELIVERED' " +
           "GROUP BY p.id, p.title " +
           "ORDER BY totalRedeem DESC")
    List<Object[]> findDeliveredStats(org.springframework.data.domain.Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(pi.totalCost),0) FROM ProductInvoice pi WHERE pi.status = 'DELIVERED'")
    java.math.BigDecimal sumDeliveredCoins();
}