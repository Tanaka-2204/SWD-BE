package com.example.demo.config;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Student;
import com.example.demo.repository.AdminRepository;   // <<< THÊM
import com.example.demo.repository.PartnerRepository; // <<< THÊM
import com.example.demo.repository.StudentRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.Set; // <<< THÊM
import java.util.stream.Collectors; // <<< THÊM

@Component
public class AuthPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    private final StudentRepository studentRepository;
    private final PartnerRepository partnerRepository; // <<< THÊM
    private final AdminRepository adminRepository;   // <<< THÊM

    // Inject 3 repositories
    public AuthPrincipalArgumentResolver(StudentRepository studentRepository,
                                         PartnerRepository partnerRepository, // <<< THÊM
                                         AdminRepository adminRepository) {  // <<< THÊM
        this.studentRepository = studentRepository;
        this.partnerRepository = partnerRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthPrincipal.class)
            && parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        // 1. Lấy đối tượng Authentication (đang bị thiếu)
        // ==========================================================
        // 🔥 THÊM DÒNG NÀY ĐỂ SỬA LỖI:
        Authentication auth = (Authentication) webRequest.getUserPrincipal();
        // ==========================================================
        
        // 2. Kiểm tra auth
        if (auth == null || !(auth instanceof JwtAuthenticationToken)) {
            return null; // Không có danh tính hoặc không phải JWT
        }

        // 3. Ép kiểu và lấy thông tin cơ bản
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth; // Dòng này bây giờ sẽ hết lỗi
        Jwt jwt = jwtAuth.getToken();
        Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();

        // 4. Lấy thông tin cơ bản VÀ DỮ LIỆU ĐỒNG BỘ
        String cognitoSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");
        String universityCode = jwt.getClaimAsString("custom:university");
        String phoneNumber = jwt.getClaimAsString("phone_number");

        // 5. "Phiên dịch" ID (Logic này giữ nguyên)
        Long studentId = null;
        Long partnerId = null;
        Long adminId = null;
        
        Set<String> roles = authorities.stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toSet());

        if (roles.contains("ROLE_ADMIN")) {
            adminId = adminRepository.findByCognitoSub(cognitoSub)
                                     .map(Admin::getId)
                                     .orElse(null);
        } else if (roles.contains("ROLE_PARTNERS")) {
            partnerId = partnerRepository.findByCognitoSub(cognitoSub)
                                         .map(Partner::getId)
                                         .orElse(null);
        } else {
            // Chỉ TÌM, không TẠO ở đây
            studentId = studentRepository.findByCognitoSub(cognitoSub)
                                         .map(Student::getId)
                                         .orElse(null);
        }

        // 6. Tạo AuthPrincipal với đầy đủ thông tin
        return new AuthPrincipal(cognitoSub, email, authorities, 
                                 fullName, universityCode, phoneNumber,
                                 studentId, partnerId, adminId);
    }
}