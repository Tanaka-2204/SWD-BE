package com.example.demo.service.impl;

import com.example.demo.dto.request.StudentProfileCompletionDTO;
import com.example.demo.dto.request.StudentProfileUpdateDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.entity.Student;
import com.example.demo.entity.University;
import com.example.demo.exception.DataIntegrityViolationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UniversityRepository;
import com.example.demo.entity.enums.UserAccountStatus; // <<< THÊM IMPORT
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;

    public StudentServiceImpl(StudentRepository studentRepository, UniversityRepository universityRepository) {
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
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

    @Transactional
    public StudentResponseDTO completeProfile(String cognitoSub, String email, StudentProfileCompletionDTO dto) {
        // 1. Kiểm tra xem student với cognitoSub này đã tồn tại chưa
        Optional<Student> existingStudentOpt = studentRepository.findByCognitoSub(cognitoSub);

        if (existingStudentOpt.isPresent()) {
            logger.warn("Profile for cognitoSub {} already exists. Updates are not allowed via this endpoint.",
                    cognitoSub);
            // Có thể trả về lỗi hoặc thông tin user đã tồn tại
            throw new DataIntegrityViolationException("Student profile already exists.");
        }

        // 2. Kiểm tra SĐT đã được sử dụng chưa
        studentRepository.findByPhoneNumber(dto.getPhoneNumber()).ifPresent(s -> {
            throw new DataIntegrityViolationException("Phone number already in use: " + dto.getPhoneNumber());
        });

        // 3. Tìm trường đại học
        University university = universityRepository.findById(dto.getUniversityId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("University not found with id: " + dto.getUniversityId()));

        // 4. Tạo student mới và liên kết với cognitoSub
        Student student = new Student();
        student.setCognitoSub(cognitoSub); // Liên kết quan trọng!
        student.setEmail(email); // Lấy từ token
        student.setUniversity(university);
        student.setFullName(dto.getFullName());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setAvatarUrl(dto.getAvatarUrl());

        Student savedStudent = studentRepository.save(student);
        logger.info("Successfully created profile for student with cognitoSub: {}", cognitoSub);

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

        University university = student.getUniversity();
        if (university != null) {
            dto.setUniversityId(university.getId());
            dto.setUniversityName(university.getName());
        }

        return dto;
    }
}