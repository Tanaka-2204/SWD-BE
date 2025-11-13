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
import com.example.demo.entity.Wallet; 
import com.example.demo.entity.WalletTransaction;
import com.example.demo.repository.WalletRepository; 
import com.example.demo.repository.WalletTransactionRepository; 
import java.math.BigDecimal;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UniversityRepository;
import com.example.demo.entity.enums.UserAccountStatus; 
import com.example.demo.exception.BadRequestException;
// === THÊM CÁC IMPORT ĐỂ UPLOAD ===
import com.example.demo.exception.InternalServerErrorException; 
import com.example.demo.service.CloudinaryService; 
import com.example.demo.service.StudentService;
import java.io.IOException; 
import org.springframework.web.multipart.MultipartFile;
// ==================================
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import java.util.Map;
import java.util.UUID;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final AWSCognitoIdentityProvider cognitoClient;
    private final WebClient webClient;
    private final CloudinaryService cloudinaryService; // <<< THÊM: Inject CloudinaryService

    @Value("${AWS_COGNITO_USER_POOL_ID}")
    private final String userPoolId;
    @Value("${cognito.userinfo-url}") 
    private String userInfoUrl;

    // <<< SỬA: Cập nhật Constructor
    public StudentServiceImpl(StudentRepository studentRepository, UniversityRepository universityRepository,
            WalletRepository walletRepository, WalletTransactionRepository transactionRepository,
            AWSCognitoIdentityProvider cognitoClient,
            WebClient.Builder webClientBuilder,
            CloudinaryService cloudinaryService) { // <<< THÊM
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.cognitoClient = cognitoClient;
        this.webClient = webClientBuilder.build();
        this.userPoolId = System.getenv("AWS_COGNITO_USER_POOL_ID");
        this.cloudinaryService = cloudinaryService; // <<< THÊM
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDTO> getAllStudents(Pageable pageable) {
        // (Giữ nguyên logic)
        logger.info("Admin fetching all students, page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Student> studentPage = studentRepository.findAll(pageable);
        return studentPage.map(this::toResponseDTO); 
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(UUID studentId) {
        // (Giữ nguyên logic)
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return toResponseDTO(student);
    }

    @Override
    @Transactional
    public StudentResponseDTO completeProfile(AuthPrincipal principal,
            String rawAccessToken,
            StudentProfileCompletionDTO completionDTO,
            MultipartFile avatarFile) {

        String cognitoSub = principal.getCognitoSub();
        if (studentRepository.findByCognitoSub(cognitoSub).isPresent()) {
            throw new DataIntegrityViolationException("Student profile already completed.");
        }
        
        Map<String, Object> userInfo;
        try {
            userInfo = webClient.get()
                    .uri(userInfoUrl)
                    .headers(headers -> headers.setBearerAuth(rawAccessToken))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Could not fetch user info from Cognito: " + e.getMessage());
        }

        if (userInfo == null) {
            throw new ResourceNotFoundException("No user info returned from Cognito.");
        }

        String fullName = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String universityName = (String) userInfo.get("custom:university");

        if (universityName == null) {
            throw new ResourceNotFoundException("University name ('custom:university') not found in token.");
        }
        University university = universityRepository.findByName(universityName)
                .orElseThrow(() -> new ResourceNotFoundException("University not found for name: " + universityName));


        Student student = new Student();
        student.setCognitoSub(cognitoSub);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setUniversity(university);
        student.setPhoneNumber(completionDTO.getPhoneNumber());

        String avatarUrl = null;
        if (avatarFile != null && !avatarFile.isEmpty()) { 
            try {
                avatarUrl = cloudinaryService.uploadFile(avatarFile); 
            } catch (IOException e) {
                throw new InternalServerErrorException("Failed to upload avatar: " + e.getMessage());
            }
        }
        student.setAvatarUrl(avatarUrl);
        // ============================================

        // ... (Logic tạo ví, tặng 100 coin, lưu Student, lưu Wallet, lưu Transaction... giữ nguyên)
        
        Wallet newWallet = new Wallet();
        BigDecimal bonusAmount = new BigDecimal(100);
        newWallet.setBalance(bonusAmount);
        newWallet.setOwnerType("STUDENT");
        newWallet.setCurrency("COIN");
        student.setWallet(newWallet);

        Student savedStudent = studentRepository.save(student);

        newWallet.setOwnerId(savedStudent.getId());
        walletRepository.save(newWallet);

        WalletTransaction bonusTx = new WalletTransaction();
        bonusTx.setWallet(newWallet);
        bonusTx.setAmount(bonusAmount); 
        bonusTx.setTxnType("SIGNUP_BONUS");
        bonusTx.setReferenceType("SYSTEM"); 
        bonusTx.setReferenceId(savedStudent.getId()); 
        transactionRepository.save(bonusTx);

        logger.info("Student {} created and received {} signup bonus.", savedStudent.getId(), bonusAmount);

        return toResponseDTO(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateMyProfile(String cognitoSub, StudentProfileUpdateDTO updateDTO, MultipartFile avatarFile) {
        logger.info("Attempting to update profile for cognitoSub: {}", cognitoSub);

        Student student = studentRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Student profile not found for the authenticated user."));

        // Cập nhật tên
        if (updateDTO.getFullName() != null) {
            student.setFullName(updateDTO.getFullName());
        }

        // Cập nhật SĐT (Logic cũ giữ nguyên)
        if (updateDTO.getPhoneNumber() != null && !updateDTO.getPhoneNumber().equals(student.getPhoneNumber())) {
            studentRepository.findByPhoneNumber(updateDTO.getPhoneNumber()).ifPresent(existingStudent -> {
                throw new DataIntegrityViolationException(
                        "Phone number " + updateDTO.getPhoneNumber() + " is already in use.");
            });
            student.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        // <<< SỬA: LOGIC UPLOAD ẢNH MỚI
        // ============================================
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String newAvatarUrl = cloudinaryService.uploadFile(avatarFile); 
                student.setAvatarUrl(newAvatarUrl);
            } catch (IOException e) {
                throw new InternalServerErrorException("Failed to update avatar: " + e.getMessage());
            }
        }

        Student updatedStudent = studentRepository.save(student);
        logger.info("Successfully updated profile for studentId: {}", updatedStudent.getId());

        return toResponseDTO(updatedStudent);
    }

    @Override
    @Transactional
    public StudentResponseDTO updateStudentStatus(UUID studentId, UserStatusUpdateDTO dto) {
        // (Giữ nguyên logic)
        logger.info("Admin updating status for studentId: {} to {}", studentId, dto.getStatus());

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        UserAccountStatus newStatus;
        try {
            newStatus = UserAccountStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + dto.getStatus());
        }

        student.setStatus(newStatus);
        Student updatedStudent = studentRepository.save(student);

        return toResponseDTO(updatedStudent);
    }

    // Helper method to convert Student Entity to StudentResponseDTO
    @Override
    public StudentResponseDTO toResponseDTO(Student student) {
        // (Giữ nguyên logic)
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

        Wallet wallet = student.getWallet();
        if (wallet != null) {
            dto.setWalletId(wallet.getId());
            dto.setBalance(wallet.getBalance());
            dto.setCurrency(wallet.getCurrency());
        }

        return dto;
    }
}