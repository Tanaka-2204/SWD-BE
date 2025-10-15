package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Lấy JWKS URI từ application.properties
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
    
    // --- Security Filter Chain ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF vì sử dụng JWT
            .authorizeHttpRequests(authorize -> authorize
                // Cho phép truy cập công khai vào Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs").permitAll() 
                // Cho phép API hoàn tất hồ sơ (cần JWT) và các API khác
                // Cần thêm phân quyền chi tiết cho các endpoint ADMIN-only
                .anyRequest().authenticated() 
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder())) // Kích hoạt xử lý JWT và dùng custom decoder
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Không dùng Session
        
        return http.build();
    }

    // --- JWT Decoder Bean ---
    /**
     * Cấu hình JWT Decoder để tìm kiếm khóa công khai của Cognito.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // NimbusJwtDecoder sẽ tự động tải các public key từ JWKS URI và xác minh chữ ký
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}