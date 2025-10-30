package com.example.demo.service.impl;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.AdminService;
import com.example.demo.service.PartnerService;
import com.example.demo.service.StudentService;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final StudentService studentService;
    private final PartnerService partnerService;
    private final AdminService adminService;

    public UserServiceImpl(StudentService studentService,
                           PartnerService partnerService,
                           AdminService adminService) {
        this.studentService = studentService;
        this.partnerService = partnerService;
        this.adminService = adminService;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMyProfile(AuthPrincipal principal) {
        String cognitoSub = principal.getCognitoSub();
        
        // 1. Kiểm tra vai trò Admin
        if (principal.isAdmin()) {
            logger.info("Fetching profile for ADMIN: {}", cognitoSub);
            return adminService.getAdminByCognitoSub(cognitoSub);
        }
        
        // 2. Kiểm tra vai trò Partner
        // (Giả sử vai trò là 'ROLE_PARTNERS' dựa trên SecurityConfig của bạn)
        if (principal.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_PARTNERS"))) {
            logger.info("Fetching profile for PARTNER: {}", cognitoSub);
            return partnerService.getPartnerByCognitoSub(cognitoSub);
        }

        // 3. Mặc định là Student
        logger.info("Fetching profile for STUDENT: {}", cognitoSub);
        
        // Đối với Student, chúng ta cần kiểm tra xem họ đã "complete-profile" hay chưa.
        // AuthPrincipal đã chứa studentId (nếu có).
        if (principal.getStudentId() == null) {
            logger.warn("Student {} has not completed their profile.", cognitoSub);
            // Trả về lỗi 404 để client biết cần gọi API complete-profile
            throw new ResourceNotFoundException("Student profile is not completed. Please complete your profile first.");
        }
        
        // Nếu đã hoàn thành, lấy thông tin bằng ID
        return studentService.getStudentById(principal.getStudentId());
    }
}