package com.example.demo.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

/**
 * Lớp này sẽ được inject vào Controller thay vì Jwt.
 * Đã được nâng cấp để chứa ID của cả 3 vai trò (chỉ 1 trong 3 có giá trị).
 */
public class AuthPrincipal {
    private String cognitoSub;
    private String email;
    private Collection<GrantedAuthority> authorities;
    
    // ID cơ sở dữ liệu nội bộ (chỉ 1 trong 3 sẽ có giá trị)
    private Long studentId; 
    private Long partnerId;
    private Long adminId;

    public AuthPrincipal(String cognitoSub, String email, Collection<GrantedAuthority> authorities, 
                         Long studentId, Long partnerId, Long adminId) {
        this.cognitoSub = cognitoSub;
        this.email = email;
        this.authorities = authorities;
        this.studentId = studentId;
        this.partnerId = partnerId;
        this.adminId = adminId;
    }
    
    // Getters
    public String getCognitoSub() { return cognitoSub; }
    public String getEmail() { return email; }
    public Collection<GrantedAuthority> getAuthorities() { return authorities; }
    public Long getStudentId() { return studentId; }
    public Long getPartnerId() { return partnerId; }
    public Long getAdminId() { return adminId; }

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