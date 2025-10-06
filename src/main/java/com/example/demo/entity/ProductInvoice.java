package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "product_invoice")
public class ProductInvoice {
    @Id
    @ColumnDefault("nextval('product_invoice_invoice_id_seq')")
    @Column(name = "invoice_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private com.example.demo.entity.Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ColumnDefault("1")
    @Column(name = "quantity")
    private Integer quantity;

    @NotNull
    @Column(name = "total_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("'COIN'")
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Size(max = 20)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", length = 20)
    private String status;

    @Size(max = 12)
    @Column(name = "verification_code", length = 12)
    private String verificationCode;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Size(max = 100)
    @Column(name = "delivered_by", length = 100)
    private String deliveredBy;

}