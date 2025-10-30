// Tạo tệp mới: com/example/demo/config/AuthPrincipalArgumentResolver.java
package com.example.demo.config;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.util.Collection;
import java.util.Optional;

@Component
public class AuthPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    private final StudentRepository studentRepository;

    // Inject repository
    public AuthPrincipalArgumentResolver(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Báo cho Spring biết: "Resolver này chỉ chạy khi
     * tham số trong controller là @AuthenticationPrincipal và có kiểu AuthPrincipal"
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
               parameter.getParameterType().equals(AuthPrincipal.class);
    }

    /**
     * Đây là logic "phiên dịch". Spring sẽ chạy hàm này để tạo ra đối tượng.
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        // 1. Lấy đối tượng Authentication
        Authentication auth = (Authentication) webRequest.getUserPrincipal();
        if (auth == null || !(auth instanceof JwtAuthenticationToken)) {
            return null; // Không có danh tính hoặc không phải JWT
        }

        // 2. Ép kiểu về JwtAuthenticationToken
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        Jwt jwt = jwtAuth.getToken(); // Lấy Jwt
        Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities(); // Lấy Roles (đã được CognitoGroupsConverter xử lý)

        // 3. Lấy thông tin cơ bản từ Token
        String cognitoSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        // 4. "Phiên dịch": Dùng cognitoSub để tìm Student ID nội bộ
        // Đây chính là logic DB lookup
        Optional<Student> studentOpt = studentRepository.findByCognitoSub(cognitoSub);
        Long studentId = studentOpt.map(Student::getId).orElse(null); // Sẽ là null nếu chưa complete-profile

        // 5. Tạo và trả về đối tượng AuthPrincipal tùy chỉnh
        // Đối tượng này sẽ được inject vào controller của bạn
        return new AuthPrincipal(cognitoSub, email, studentId, authorities);
    }
}