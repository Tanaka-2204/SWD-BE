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
import java.util.Optional;
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

    public StudentServiceImpl(StudentRepository studentRepository, UniversityRepository universityRepository,
            WalletRepository walletRepository) {
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
        this.walletRepository = walletRepository;
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

        // 1. Lấy dữ liệu từ Principal (đã được đồng bộ từ JWT)
        String cognitoSub = principal.getCognitoSub();
        String email = principal.getEmail();
        String fullName = principal.getFullName(); // <<< TỪ PRINCIPAL
        String universityCode = principal.getUniversityCode(); // <<< TỪ PRINCIPAL

        // 2. Kiểm tra xem profile đã tồn tại chưa
        studentRepository.findByCognitoSub(cognitoSub).ifPresent(s -> {
            throw new DataIntegrityViolationException("Student profile already exists.");
        });

        // 3. Lấy dữ liệu còn lại từ DTO
        String phoneNumber = dto.getPhoneNumber(); // <<< TỪ DTO
        String avatarUrl = dto.getAvatarUrl(); // <<< TỪ DTO

        // 4. Validate dữ liệu Cognito (phòng trường hợp token rỗng)
        if (universityCode == null || universityCode.isBlank()) {
            throw new BadRequestException("University code is missing from Cognito token (custom:university).");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new BadRequestException("Full name is missing from Cognito token (name).");
        }

        // 5. Kiểm tra SĐT (từ DTO)
        studentRepository.findByPhoneNumber(phoneNumber).ifPresent(s -> {
            throw new DataIntegrityViolationException("Phone number " + phoneNumber + " already in use.");
        });

        // 6. Tìm University bằng "Code" lấy từ Principal
        University university = universityRepository.findByCode(universityCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "University not found for code: " + universityCode +
                                ". Please contact admin to add this university code."));

        // 7. Tạo Student mới
        Student student = new Student();
        student.setCognitoSub(cognitoSub);
        student.setEmail(email);
        student.setFullName(fullName); // (từ Principal)
        student.setUniversity(university); // (tìm được từ Principal)
        student.setPhoneNumber(phoneNumber); // (từ DTO)
        student.setAvatarUrl(avatarUrl); // (từ DTO)
        student.setStatus(UserAccountStatus.ACTIVE);

        Student savedStudent = studentRepository.save(student);
        logger.info("Successfully created profile for student with cognitoSub: {}", cognitoSub);

        // 8. TẠO VÍ (Wallet) cho sinh viên mới
        logger.info("Creating wallet for new studentId: {}", savedStudent.getId());
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