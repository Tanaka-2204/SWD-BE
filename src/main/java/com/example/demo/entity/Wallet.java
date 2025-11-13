package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "wallet", uniqueConstraints = { // THÊM uniqueConstraints
        @UniqueConstraint(columnNames = { "owner_type", "owner_id", "currency" })
})
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // SỬA ĐỔI CHIẾN LƯỢC
    @Column(name = "wallet_id", nullable = false)
    private UUID id;

    @Size(max = 20)
    @Column(name = "owner_type", length = 20)
    private String ownerType;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("'COIN'")
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    @Version
    private Integer version;

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false) 
    private OffsetDateTime createdAt;

}