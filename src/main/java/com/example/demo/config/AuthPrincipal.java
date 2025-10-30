// com/example/demo/config/AuthPrincipal.java
package com.example.demo.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

// Lớp này sẽ được inject vào Controller thay vì Jwt
public class AuthPrincipal {
    private String cognitoSub;
    private String email;
    private Long studentId; // Sẽ là null nếu chưa complete-profile
    private Collection<GrantedAuthority> authorities;

    public AuthPrincipal(String cognitoSub, String email, Long studentId, Collection<GrantedAuthority> authorities) {
        this.cognitoSub = cognitoSub;
        this.email = email;
        this.studentId = studentId;
        this.authorities = authorities;
    }

    // Thêm các getter...
    public String getCognitoSub() { return cognitoSub; }
    public String getEmail() { return email; }
    public Long getStudentId() { return studentId; }
    public Collection<GrantedAuthority> getAuthorities() { return authorities; }
    public boolean isAdmin() {
        return authorities.stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
    }
}