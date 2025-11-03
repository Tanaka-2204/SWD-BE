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
    
    List<ProductInvoice> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT pi FROM ProductInvoice pi WHERE pi.student.id = :studentId " +
           "AND (:status IS NULL OR pi.status = :status)")
    Page<ProductInvoice> findInvoicesByStudent(@Param("studentId") Long studentId, 
                                                @Param("status") String status, 
                                                Pageable pageable);

    @Query("SELECT COUNT(pi), COALESCE(SUM(pi.totalCost), 0) FROM ProductInvoice pi")
    List<Object[]> getInvoiceStats();

    @Query("SELECT p.title, COUNT(pi) FROM ProductInvoice pi JOIN pi.product p " +
           "GROUP BY p.title ORDER BY COUNT(pi) DESC")
    List<Object[]> getTopProducts();
}