package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "admin_user")
public class AdminUser {
    @Id
    @ColumnDefault("nextval('admin_user_admin_id_seq')")
    @Column(name = "admin_id", nullable = false)
    private Long id;

    @Size(max = 200)
    @Column(name = "email", length = 200)
    private String email;

    @Size(max = 200)
    @Column(name = "full_name", length = 200)
    private String fullName;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}