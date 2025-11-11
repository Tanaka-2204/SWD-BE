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
import com.example.demo.entity.WalletTransaction;
import com.example.demo.repository.WalletRepository; // <<< THÊM IMPORT
import com.example.demo.repository.WalletTransactionRepository; // <<< THÊM IMPORT
import java.math.BigDecimal;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UniversityRepository;
import com.example.demo.entity.enums.UserAccountStatus; // <<< THÊM IMPORT
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.StudentService;
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
    @Value("${AWS_COGNITO_USER_POOL_ID}")
    private final String userPoolId;
    @Value("${cognito.userinfo-url}") // <<< THÊM
    private String userInfoUrl;

    public StudentServiceImpl(StudentRepository studentRepository, UniversityRepository universityRepository,
            WalletRepository walletRepository, WalletTransactionRepository transactionRepository,
            AWSCognitoIdentityProvider cognitoClient,
            WebClient.Builder webClientBuilder) {
        this.studentRepository = studentRepository;
        this.universityRepository = universityRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.cognitoClient = cognitoClient;
        this.webClient = webClientBuilder.build();
        this.userPoolId = System.getenv("AWS_COGNITO_USER_POOL_ID");
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
    public StudentResponseDTO getStudentById(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        return toResponseDTO(student);
    }

    @Override
    @Transactional
    public StudentResponseDTO completeProfile(AuthPrincipal principal,
            String rawAccessToken,
            StudentProfileCompletionDTO completionDTO) {

        // 1. Kiểm tra profile (Logic cũ)
        String cognitoSub = principal.getCognitoSub();
        if (studentRepository.findByCognitoSub(cognitoSub).isPresent()) {
            throw new DataIntegrityViolationException("Student profile already completed.");
        }

        // 2. Gọi API /USERINFO (Logic cũ)
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

        // 3. Lấy thông tin từ /userinfo (Logic cũ)
        String fullName = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String universityName = (String) userInfo.get("custom:university");

        if (universityName == null) {
            // Lỗi này sẽ xuất hiện nếu claim thực sự không có trong token
            throw new ResourceNotFoundException("University name ('custom:university') not found in token.");
        }
        // 4. Tìm trường (Logic cũ)
        University university = universityRepository.findByName(universityName)
                .orElseThrow(() -> new ResourceNotFoundException("University not found for name: " + universityName));

        // 5. Trộn dữ liệu và tạo Student (Logic cũ)
        Student student = new Student();
        student.setCognitoSub(cognitoSub);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setUniversity(university);
        student.setPhoneNumber(completionDTO.getPhoneNumber());
        student.setAvatarUrl(completionDTO.getAvatarUrl());

        // ==========================================================
        // <<< LOGIC TẶNG 100 COIN KHI ĐĂNG KÝ
        // ==========================================================

        // 6. Tạo ví
        Wallet newWallet = new Wallet();

        // 6.1. SỬA ĐỔI: Set số dư ban đầu là 100
        BigDecimal bonusAmount = new BigDecimal(100);
        newWallet.setBalance(bonusAmount);
        newWallet.setOwnerType("STUDENT");
        newWallet.setCurrency("COIN");
        // 7. Gán Ví vào Student (Logic cũ)
        student.setWallet(newWallet);

        // 8. Lưu Student (CascadeType.ALL sẽ tự động lưu cả Wallet)
        Student savedStudent = studentRepository.save(student);

        // 9. Cập nhật ownerId cho Wallet (Logic cũ)
        newWallet.setOwnerId(savedStudent.getId());
        walletRepository.save(newWallet); // (Lưu lại Wallet để có ID)

        // 10. THÊM MỚI: Ghi lại giao dịch tặng thưởng
        WalletTransaction bonusTx = new WalletTransaction();
        bonusTx.setWallet(newWallet);
        bonusTx.setAmount(bonusAmount); // Số tiền dương (cộng vào)
        bonusTx.setTxnType("SIGNUP_BONUS"); // Loại giao dịch: Thưởng đăng ký
        bonusTx.setReferenceType("SYSTEM"); // Tham chiếu: Hệ thống
        bonusTx.setReferenceId(savedStudent.getId()); // Ghi ID sinh viên cho dễ truy vết

        transactionRepository.save(bonusTx);

        logger.info("Student {} created and received {} signup bonus.", savedStudent.getId(), bonusAmount);
        // ==========================================================

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
    public StudentResponseDTO updateStudentStatus(UUID studentId, UserStatusUpdateDTO dto) {
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
    @Override
    public StudentResponseDTO toResponseDTO(Student student) {
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

        // (Thêm logic ví đã làm trước đó)
        Wallet wallet = student.getWallet();
        if (wallet != null) {
            dto.setWalletId(wallet.getId());
            dto.setBalance(wallet.getBalance());
            dto.setCurrency(wallet.getCurrency());
        }

        return dto;
    }
}
