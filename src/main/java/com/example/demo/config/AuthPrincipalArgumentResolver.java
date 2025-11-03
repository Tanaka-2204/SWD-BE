package com.example.demo.config;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Student;
import com.example.demo.repository.AdminRepository; // <<< THÊM
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
import java.util.Optional; // <<< THÊM
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    private final StudentRepository studentRepository;
    private final PartnerRepository partnerRepository; // <<< THÊM
    private final AdminRepository adminRepository; // <<< THÊM
    private static final Logger logger = LoggerFactory.getLogger(AuthPrincipalArgumentResolver.class);
    // Inject 3 repositories
    public AuthPrincipalArgumentResolver(StudentRepository studentRepository,
            PartnerRepository partnerRepository, // <<< THÊM
            AdminRepository adminRepository) { // <<< THÊM
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
        
        logger.info("--- DEBUG RESOLVER: Starting AuthPrincipal resolution ---");

        Authentication auth = (Authentication) webRequest.getUserPrincipal();
        if (auth == null || !(auth instanceof JwtAuthenticationToken)) {
            logger.warn("--- DEBUG RESOLVER: Authentication is null or not JwtAuthenticationToken ---");
            return null;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        Jwt jwt = jwtAuth.getToken();
        Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
        Set<String> roles = authorities.stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toSet());

        String cognitoSub = jwt.getSubject();
        logger.info("--- DEBUG RESOLVER: Cognito Sub (from Token) = '{}'", cognitoSub);
        logger.info("--- DEBUG RESOLVER: Roles (from Token) = {}", roles);

        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");
        String universityCode = jwt.getClaimAsString("custom:university");
        String phoneNumber = jwt.getClaimAsString("phone_number");
        String username = jwt.getClaimAsString("username");
        if (username == null) {
            username = email;
        }

        Long studentId = null;
        Long partnerId = null;
        Long adminId = null;

        if (roles.contains("ROLE_ADMIN")) {
            logger.info("--- DEBUG RESOLVER: User is ADMIN. Searching admin table...");
            Optional<Admin> admin = adminRepository.findByCognitoSub(cognitoSub);
            adminId = admin.map(Admin::getId).orElse(null);
            logger.info("--- DEBUG RESOLVER: Admin ID found = {}", adminId);

        } else if (roles.contains("ROLE_PARTNERS")) {
            logger.info("--- DEBUG RESOLVER: User is PARTNER. Searching partner table...");
            // <<< LOGIC TÌM KIẾM PARTNER ĐANG GẶP VẤN ĐỀ
            Optional<Partner> partner = partnerRepository.findByCognitoSub(cognitoSub);
            partnerId = partner.map(Partner::getId).orElse(null);
            logger.info("--- DEBUG RESOLVER: Partner ID found = {}", partnerId); // <<< HÃY XEM GIÁ TRỊ NÀY LÀ GÌ

        } else { // (Logic cho Student, đã sửa)
            if (!roles.isEmpty()) { // (Nếu không phải Admin/Partner nhưng có role lạ)
                logger.warn("--- DEBUG RESOLVER: User has unhandled roles: {}", roles);
            }
            logger.info("--- DEBUG RESOLVER: User is STUDENT. Searching student table...");
            Optional<Student> student = studentRepository.findByCognitoSub(cognitoSub);
            studentId = student.map(Student::getId).orElse(null);
            logger.info("--- DEBUG RESOLVER: Student ID found = {}", studentId);
        }

        logger.info("--- DEBUG RESOLVER: Final IDs: [Student={}, Partner={}, Admin={}] ---", studentId, partnerId, adminId);
        
        return new AuthPrincipal(cognitoSub, username, email, authorities,
                fullName, universityCode, phoneNumber,
                studentId, partnerId, adminId);
    }
}