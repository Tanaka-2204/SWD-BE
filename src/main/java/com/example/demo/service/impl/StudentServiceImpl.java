package com.example.demo.service.impl;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.entity.Student;
import com.example.demo.entity.University;
import com.example.demo.exception.DataIntegrityViolationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Wallet; // <<< THÊM IMPORT
import com.example.demo.repository.WalletRepository; // <<< THÊM IMPORT
import java.math.BigDecimal;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UniversityRepository;
import com.example.demo.entity.enums.UserAccountStatus; // <<< THÊM IMPORT
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;
    private final WalletRepository walletRepository;
    private final AWSCognitoIdentityProvider cognitoClient;
    private final String userPoolId = "ap-southeast-2_9RLjNQhOk";

    public StudentServiceImpl(StudentRepository studentRepository, UniversityRepository universityRepository,
            WalletRepository walletRepository, AWSCognitoIdentityProvider cognitoClient) {
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
        this.walletRepository = walletRepository;
        this.cognitoClient = cognitoClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDTO> getAllStudents(Pageable pageable) {
        logger.info("Admin fetching all students, page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Student> studentPage = studentRepository.findAll(pageable);
        return studentPage.map(this::toResponseDTO); // Dùng lại helper method
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return toResponseDTO(student);
    }

    @Override
    @Transactional
    public StudentResponseDTO completeProfile(AuthPrincipal principal, StudentProfileCompletionDTO dto) {

        String cognitoSub = principal.getCognitoSub();
        String cognitoUsername = principal.getUsername();
        String userEmail = principal.getEmail();
        studentRepository.findByCognitoSub(cognitoSub).ifPresent(s -> {
            throw new DataIntegrityViolationException("Student profile already exists.");
        });

        // 3. Lấy dữ liệu còn lại từ DTO (logic giữ nguyên)
        String phoneNumber = dto.getPhoneNumber();
        String avatarUrl = dto.getAvatarUrl();

        // ==========================================================
        // SỬA ĐỔI QUAN TRỌNG: TRUY VẤN COGNITO ĐỂ LẤY DỮ LIỆU MỚI NHẤT
        // ==========================================================
        String universityCodeOrName;
        String fullName;

        try {
            AdminGetUserRequest getUserRequest = new AdminGetUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(cognitoUsername); // Sử dụng cognitoSub

            AdminGetUserResult userResult = cognitoClient.adminGetUser(getUserRequest);

            // Trích xuất University Code và Full Name từ User Attributes
            universityCodeOrName = userResult.getUserAttributes().stream()
                    .filter(attr -> "custom:university".equals(attr.getName()))
                    .findFirst().map(AttributeType::getValue)
                    .orElse(null);

            fullName = userResult.getUserAttributes().stream()
                    .filter(attr -> "name".equals(attr.getName()))
                    .findFirst().map(AttributeType::getValue)
                    .orElse(null);

        } catch (Exception e) {
            logger.error("Error retrieving user attributes from Cognito for sub {}: {}", cognitoSub, e.getMessage());
            throw new ResourceNotFoundException("Error accessing user data from Cognito.");
        }

        // 4. Validate dữ liệu Cognito MỚI (chỉ còn lại trường hợp Lambda thất bại)
        if (universityCodeOrName == null || universityCodeOrName.isBlank()) {
            // Lỗi này xảy ra khi Lambda Post-Confirmation GHI DỮ LIỆU THẤT BẠI
            throw new BadRequestException("University code is missing from Cognito attributes. Please contact admin.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new BadRequestException("Full name is missing from Cognito attributes.");
        }
        // (Bỏ qua việc kiểm tra DataIntegrity của Phone Number, logic giữ nguyên)
        studentRepository.findByPhoneNumber(phoneNumber).ifPresent(s -> {
            throw new DataIntegrityViolationException("Phone number " + phoneNumber + " already in use.");
        });

        // 5. Tìm University (logic giữ nguyên)
        University university = universityRepository.findByName(universityCodeOrName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "University not found for name: " + universityCodeOrName +
                                ". Please contact admin to add this university to the database."));

        // 6. Tạo Student mới (logic giữ nguyên)
        Student student = new Student();
        student.setCognitoSub(cognitoSub);
        student.setEmail(userEmail);
        student.setFullName(fullName); // <<< DÙNG FULLNAME MỚI TỪ COGNITO
        student.setUniversity(university);
        student.setPhoneNumber(phoneNumber);
        student.setAvatarUrl(avatarUrl);
        student.setStatus(UserAccountStatus.ACTIVE);

        // 7. Tạo Wallet, Save Student và trả về (logic giữ nguyên)
        Student savedStudent = studentRepository.save(student);
        // ... (Wallet creation logic)
        Wallet studentWallet = new Wallet();
        studentWallet.setOwnerType("STUDENT");
        studentWallet.setOwnerId(savedStudent.getId());
        studentWallet.setBalance(BigDecimal.ZERO);
        studentWallet.setCurrency("COIN");
        walletRepository.save(studentWallet);

        return toResponseDTO(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateMyProfile(String cognitoSub, StudentProfileUpdateDTO updateDTO) {
        logger.info("Attempting to update profile for cognitoSub: {}", cognitoSub);

        // 1. Tìm student bằng cognitoSub thay vì studentId
        Student student = studentRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Student profile not found for the authenticated user."));

        // 2. Cập nhật các trường được phép
        if (updateDTO.getFullName() != null) {
            student.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getAvatarUrl() != null) {
            student.setAvatarUrl(updateDTO.getAvatarUrl());
        }

        // 3. Logic cập nhật SĐT giữ nguyên (vẫn cần kiểm tra trùng lặp)
        if (updateDTO.getPhoneNumber() != null && !updateDTO.getPhoneNumber().equals(student.getPhoneNumber())) {
            studentRepository.findByPhoneNumber(updateDTO.getPhoneNumber()).ifPresent(existingStudent -> {
                throw new DataIntegrityViolationException(
                        "Phone number " + updateDTO.getPhoneNumber() + " is already in use.");
            });
            student.setPhoneNumber(updateDTO.getPhoneNumber());
        }

        // 4. KHÔNG CÓ LOGIC CẬP NHẬT EMAIL Ở ĐÂY

        Student updatedStudent = studentRepository.save(student);
        logger.info("Successfully updated profile for studentId: {}", updatedStudent.getId());

        return toResponseDTO(updatedStudent);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateStudentStatus(Long studentId, UserStatusUpdateDTO dto) {
        logger.info("Admin updating status for studentId: {} to {}", studentId, dto.getStatus());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        UserAccountStatus newStatus;
        try {
            // Chuyển String ("ACTIVE") thành Enum (UserAccountStatus.ACTIVE)
            newStatus = UserAccountStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + dto.getStatus());
        }

        student.setStatus(newStatus);
        Student updatedStudent = studentRepository.save(student);

        return toResponseDTO(updatedStudent);
    }

    // Helper method to convert Student Entity to StudentResponseDTO
    StudentResponseDTO toResponseDTO(Student student) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setFullName(student.getFullName());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setEmail(student.getEmail());
        dto.setAvatarUrl(student.getAvatarUrl());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setStatus(student.getStatus().name());

        University university = student.getUniversity();
        if (university != null) {
            dto.setUniversityId(university.getId());
            dto.setUniversityName(university.getName());
        }

        return dto;
    }
}