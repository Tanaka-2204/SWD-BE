package com.example.demo.entity;

// <<< THÊM IMPORT NÀY
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.example.demo.entity.enums.UserAccountStatus;
import java.util.UUID;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "Partner") 
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "partner_id", nullable = false)
    private UUID id;

    @Column(name = "cognito_sub", unique = true, nullable = true, updatable = false) 
    private String cognitoSub;

    @Size(max = 200)
    @NotNull
    @Column(name = "name", nullable = false, length = 200, unique = true) 
    private String name;

    @Size(max = 50)
    @Column(name = "organization_type", length = 50)
    private String organizationType;

    @Size(max = 200)
    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Size(max = 20)
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id", unique = true) 
    private Wallet wallet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    private UserAccountStatus status = UserAccountStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

}