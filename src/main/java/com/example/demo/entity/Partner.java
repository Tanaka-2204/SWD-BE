package com.example.demo.entity;

// <<< THÊM IMPORT NÀY
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import com.example.demo.entity.enums.UserAccountStatus; // <<< THÊM IMPORT NÀY

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "Partner") // Giữ nguyên tên bảng là 'partner' nếu chưa đổi trong DB
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partner_id", nullable = false)
    private Long id;

    // --- THÊM TRƯỜNG cognito_sub ---
    @Column(name = "cognito_sub", unique = true, nullable = true, updatable = false) // Cho phép NULL ban đầu nếu cần
    private String cognitoSub;
    // ----------------------------

    @Size(max = 200)
    @NotNull
    @Column(name = "name", nullable = false, length = 200, unique = true) // Thêm unique = true cho name
    private String name;

    @Size(max = 50)
    @Column(name = "organization_type", length = 50)
    private String organizationType;

    @Size(max = 200)
    @Column(name = "contact_email", length = 200) // Cân nhắc thêm unique = true
    private String contactEmail;

    @Size(max = 20)
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // Đổi thành OneToOne và CascadeType.ALL nếu Wallet luôn đi kèm Partner
    @JoinColumn(name = "wallet_id", unique = true) // Thêm unique = true cho wallet_id
    private Wallet wallet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    private UserAccountStatus status = UserAccountStatus.ACTIVE;
    
    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}