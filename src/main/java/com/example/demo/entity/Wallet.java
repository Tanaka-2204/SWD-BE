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
@Table(name = "wallet")
public class Wallet {
    @Id
    @ColumnDefault("nextval('wallet_wallet_id_seq')")
    @Column(name = "wallet_id", nullable = false)
    private Long id;

    @Size(max = 20)
    @Column(name = "owner_type", length = 20)
    private String ownerType;

    @Column(name = "owner_id")
    private Long ownerId;

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
    private Integer version;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}