package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor 
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "student_id", updatable = false, nullable = false)
    private UUID studentId;

    @NotNull
    @Column(name = "cognito_sub", unique = true, nullable = false)
    private String cognitoSub;
    
    @NotNull
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @NotNull
    @Column(name = "qr_code_identifier", unique = true, nullable = false, length = 50)
    private String qrCodeIdentifier;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Column(name = "point_balance")
    private Integer pointBalance;
    
}
