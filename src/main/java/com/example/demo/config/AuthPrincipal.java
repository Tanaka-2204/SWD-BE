package com.example.demo.config;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;

/**
 * Lớp này sẽ được inject vào Controller thay vì Jwt.
 * Chứa thông tin đồng bộ từ Cognito và ID của cả 3 vai trò.
 */
@SuppressWarnings("serial")
public class AuthPrincipal {
    private String cognitoSub;
    private String username;
    private String email;
    private Collection<GrantedAuthority> authorities;
    
    // Dữ liệu đồng bộ từ JWT (Cognito Attributes)
    private String fullName;
    private String universityCode; // (từ custom:university)
    private String phoneNumber;    // (từ phone_number)

    // ID cơ sở dữ liệu nội bộ (chỉ 1 trong 3 sẽ có giá trị)
    private UUID studentId; 
    private UUID partnerId;
    private UUID adminId;

    /**
     * Đây là hàm khởi tạo (constructor) 9 tham số mà Resolver đang gọi.
     * Hãy đảm bảo tệp của bạn có chính xác hàm này.
     */
    public AuthPrincipal(String cognitoSub, String username, String email, Collection<GrantedAuthority> authorities, 
                         String fullName, String universityCode, String phoneNumber,
                         UUID studentId, UUID partnerId, UUID adminId) {
        this.cognitoSub = cognitoSub;
        this.username = username;
        this.email = email;
        this.authorities = authorities;
        this.fullName = fullName;
        this.universityCode = universityCode;
        this.phoneNumber = phoneNumber;
        this.studentId = studentId;
        this.partnerId = partnerId;
        this.adminId = adminId;
    }
    
    // Getters
    public String getCognitoSub() { return cognitoSub; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Collection<GrantedAuthority> getAuthorities() { return authorities; }
    public String getFullName() { return fullName; }
    public String getUniversityCode() { return universityCode; }
    public String getPhoneNumber() { return phoneNumber; }
    public UUID getStudentId() { return studentId; }
    public UUID getPartnerId() { return partnerId; }
    public UUID getAdminId() { return adminId; }

    // Helper methods để kiểm tra vai trò
    public boolean hasRole(String roleName) {
        return authorities.stream().anyMatch(ga -> ga.getAuthority().equals(roleName));
    }
    
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    public boolean isPartner() {
        return hasRole("ROLE_PARTNERS");
    }
    
    // Sinh viên là người không phải Admin hoặc Partner
    public boolean isStudent() {
        return !isAdmin() && !isPartner();
    }
}