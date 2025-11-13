package com.example.demo.entity;

import com.example.demo.entity.enums.UserAccountStatus;
import com.example.demo.validation.annotations.VietnamesePhoneNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.util.UUID;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_id", nullable = false)
    private UUID id;

    @Column(name = "cognito_sub", unique = true, nullable = false, updatable = false)
    private String cognitoSub;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Size(max = 200)
    @NotNull
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Size(max = 20)
    @NotNull
    @Column(name = "phone_number", nullable = false, length = 20, unique = true)
    @VietnamesePhoneNumber
    private String phoneNumber;

    @Size(max = 200)
    @Column(name = "email", length = 200, unique = true) // Cân nhắc thêm unique = true
    private String email;

    @Column(name = "avatar_url") // Loại bỏ length
    private String avatarUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    private UserAccountStatus status = UserAccountStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", referencedColumnName = "wallet_id", nullable = false, unique = true)
    private Wallet wallet;

}