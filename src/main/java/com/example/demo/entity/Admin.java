package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "Admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SỬA ĐỔI NÀY
    @Column(name = "admin_id", nullable = false)
    private Long id;

    @Column(name = "cognito_sub", unique = true, nullable = false, updatable = false)
    private String cognitoSub;

    @Size(max = 200)
    @Column(name = "email", length = 200, unique = true)
    private String email;

    @Size(max = 200)
    @Column(name = "full_name", length = 200)
    private String fullName;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}