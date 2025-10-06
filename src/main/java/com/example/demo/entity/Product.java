package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "product")
public class Product {
    @Id
    @ColumnDefault("nextval('product_product_id_seq')")
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Size(max = 20)
    @Column(name = "type", length = 20)
    private String type;

    @Size(max = 200)
    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @NotNull
    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("'COIN'")
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    @Column(name = "image_url", length = Integer.MAX_VALUE)
    private String imageUrl;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}