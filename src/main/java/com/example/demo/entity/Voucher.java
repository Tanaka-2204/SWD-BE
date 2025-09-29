package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "voucher_id")
    private UUID voucherId;

    @Column(name = "name")
    private String name;

    @Column(name = "point_cost", nullable = false)
    private Integer pointCost;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "expiry_date")
    private Date expiryDate;
}