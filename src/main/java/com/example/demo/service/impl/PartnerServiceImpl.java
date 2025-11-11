package com.example.demo.service.impl;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.request.UserStatusUpdateDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Wallet;
import com.example.demo.entity.enums.UserAccountStatus;
import com.example.demo.exception.DataIntegrityViolationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.service.PartnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AliasExistsException;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.example.demo.exception.BadRequestException;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID; // <<< THÊM IMPORT

@Service
public class PartnerServiceImpl implements PartnerService {

    private static final Logger logger = LoggerFactory.getLogger(PartnerServiceImpl.class); // Thêm logger

    private final PartnerRepository partnerRepository;
    private final WalletRepository walletRepository;
    private final AWSCognitoIdentityProvider cognitoClient; // <<< GIỮ LẠI KHAI BÁO NÀY

    @Value("${AWS_COGNITO_USER_POOL_ID}") // <<< GIỮ LẠI KHAI BÁO NÀY
    private String userPoolId;

    public PartnerServiceImpl(PartnerRepository partnerRepository,
            WalletRepository walletRepository,
            @Value("${AWS_ACCESS_KEY_ID}") String accessKey,
            @Value("${AWS_SECRET_ACCESS_KEY}") String secretKey,
            @Value("${AWS_REGION}") String awsRegion) {
        this.partnerRepository = partnerRepository;
        this.walletRepository = walletRepository;

        // TẠO CREDENTIALS TƯỜNG MINH TỪ PROPERTIES
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);

        // Khởi tạo Cognito Client với credentials và region đã inject
        this.cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(awsRegion)
                .build();
    }

    @Override
    @Transactional
    public PartnerResponseDTO createPartner(PartnerRequestDTO requestDTO) {
        // ===>>> LẤY USERNAME TỪ DTO <<<===
        String partnerCognitoUsername = requestDTO.getUsername();
        String partnerEmail = requestDTO.getContactEmail();
        logger.info("Attempting to create partner and Cognito user with username: {}", partnerCognitoUsername);

        // 1. Kiểm tra tên đối tác trong DB
        partnerRepository.findByName(requestDTO.getName()).ifPresent(p -> {
            throw new DataIntegrityViolationException(
                    "Partner with name '" + requestDTO.getName() + "' already exists.");
        });

        // ================================================
        // == BƯỚC 2: TẠO USER MỚI TRÊN COGNITO ==
        // ================================================
        String createdCognitoSub = null;
        AdminAddUserToGroupRequest addUserToGroupRequest = new AdminAddUserToGroupRequest()
                .withUserPoolId(userPoolId)
                .withUsername(partnerCognitoUsername)
                .withGroupName("PARTNERS");
        try {
            AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(partnerCognitoUsername)
                    .withUserAttributes(
                            new AttributeType().withName("email").withValue(partnerEmail),
                            new AttributeType().withName("name").withValue(requestDTO.getName()),
                            new AttributeType().withName("email_verified").withValue("true"),
                            new AttributeType().withName("custom:user_type").withValue("PARTNERS"))
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .withForceAliasCreation(Boolean.FALSE);

            AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(createUserRequest);

            // Lấy Sub ID của user MỚI TẠO
            createdCognitoSub = createUserResult.getUser().getAttributes().stream()
                    .filter(attr -> "sub".equals(attr.getName()))
                    .map(AttributeType::getValue)
                    .findFirst().orElseThrow(() -> new RuntimeException("Cognito user created but 'sub' not found."));

            logger.info("Successfully created Cognito user: {} with sub: {}", partnerCognitoUsername,
                    createdCognitoSub);

            // Thêm user MỚI TẠO vào group
            cognitoClient.adminAddUserToGroup(addUserToGroupRequest);
            logger.info("Successfully added new Cognito user {} to group PARTNERS", partnerCognitoUsername);

        } catch (UsernameExistsException e) {
            // === USER ĐÃ TỒN TẠI ===
            logger.warn("Cognito username {} already exists. Getting user SUB and adding to group.",
                    partnerCognitoUsername);

            try {
                // BƯỚC 1: LẤY THÔNG TIN USER ĐÃ TỒN TẠI
                AdminGetUserRequest getUserRequest = new AdminGetUserRequest()
                        .withUserPoolId(userPoolId)
                        .withUsername(partnerCognitoUsername);

                AdminGetUserResult getUserResult = cognitoClient.adminGetUser(getUserRequest);

                // Lấy Sub ID của user ĐÃ TỒN TẠI
                createdCognitoSub = getUserResult.getUserAttributes().stream()
                        .filter(attr -> "sub".equals(attr.getName()))
                        .map(AttributeType::getValue)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Cognito user exists but 'sub' not found."));

                // BƯỚC 2: THÊM USER ĐÃ TỒN TẠI VÀO GROUP
                cognitoClient.adminAddUserToGroup(addUserToGroupRequest);
                logger.info("Successfully added existing Cognito user {} to group PARTNERS", partnerCognitoUsername);

            } catch (UserNotFoundException notFoundE) {
                // Mâu thuẫn logic: "Exists" xong lại "Not Found"
                logger.error("Cognito logic error: User {} reported 'Exists' but then 'Not Found'. {}",
                        partnerCognitoUsername, notFoundE.getMessage());
                throw new RuntimeException("Cognito state contradiction. Please check user status.", notFoundE);

            } catch (Exception addGroupError) {
                // Lỗi khi thêm vào group (ví dụ: user đã ở trong group)
                logger.error("Failed to add existing Cognito user {} to group PARTNERS: {}", partnerCognitoUsername,
                        addGroupError.getMessage());
                throw new RuntimeException("Failed to assign partner role in Cognito for existing user.",
                        addGroupError);
            }

        } catch (AliasExistsException e) {
            logger.error("Cognito user with email alias {} already exists.", partnerEmail, e);
            throw new DataIntegrityViolationException("Partner email already exists in Cognito.", e);

        } catch (InvalidParameterException e) {
            // Lỗi này xảy ra nếu username/password không hợp lệ
            logger.error("Invalid parameter creating Cognito user {}: {}", partnerCognitoUsername, e.getMessage());
            throw new BadRequestException(
                    "Invalid username or attributes for Cognito (e.g., password policy): " + e.getMessage(), e);

        } catch (Exception e) {
            // Bắt các lỗi khác
            logger.error("Failed to create Cognito user {} or add to group: {}", partnerCognitoUsername, e.getMessage(),
                    e);
            throw new RuntimeException("Failed to create Cognito user or assign role. Partner creation rolled back.",
                    e);
        }

        // Đảm bảo createdCognitoSub KHÔNG BAO GIỜ null ở đây
        if (createdCognitoSub == null) {
            logger.error("CRITICAL: createdCognitoSub is null after try-catch blocks. Rolling back.");
            throw new RuntimeException("Failed to retrieve Cognito SUB ID for user " + partnerCognitoUsername);
        }

        // ================================================

        // 3. Tạo Wallet và Partner trong Database (Giữ nguyên)
        Partner partner = new Partner();
        partner.setName(requestDTO.getName());
        partner.setOrganizationType(requestDTO.getOrganizationType());
        partner.setContactEmail(requestDTO.getContactEmail());
        partner.setContactPhone(requestDTO.getContactPhone());
        partner.setCognitoSub(createdCognitoSub); 

        Wallet wallet = new Wallet();
        wallet.setOwnerType("PARTNER");
        wallet.setCurrency("COIN");
        wallet.setBalance(BigDecimal.ZERO);
        Wallet savedWallet = walletRepository.save(wallet);

        partner.setWallet(savedWallet);
        Partner savedPartner = partnerRepository.save(partner);

        savedWallet.setOwnerId(savedPartner.getId());
        walletRepository.save(savedWallet);

        logger.info("Successfully created partner {} with ID {} in database", savedPartner.getName(),
                savedPartner.getId());
        return convertToDTO(savedPartner);
    }

    // --- Các phương thức GET, UPDATE, DELETE giữ nguyên ---
    @Override
    @Transactional(readOnly = true)
    public PartnerResponseDTO getPartnerById(UUID partnerId) { // SỬA: Long -> UUID
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        return convertToDTO(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponseDTO getPartnerByCognitoSub(String cognitoSub) {
        Partner partner = partnerRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found for authenticated user."));
        return convertToDTO(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerResponseDTO> getAllPartners(Pageable pageable) {
        return partnerRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public PartnerResponseDTO updatePartner(UUID partnerId, PartnerRequestDTO requestDTO) { // SỬA: Long -> UUID
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));

        // Kiểm tra tên mới có trùng không (nếu tên thay đổi)
        if (requestDTO.getName() != null && !requestDTO.getName().equals(partner.getName())) {
            partnerRepository.findByName(requestDTO.getName()).ifPresent(p -> {
                throw new DataIntegrityViolationException(
                        "Partner with name '" + requestDTO.getName() + "' already exists.");
            });
            partner.setName(requestDTO.getName());
        }

        if (requestDTO.getOrganizationType() != null)
            partner.setOrganizationType(requestDTO.getOrganizationType());
        if (requestDTO.getContactEmail() != null)
            partner.setContactEmail(requestDTO.getContactEmail());
        if (requestDTO.getContactPhone() != null)
            partner.setContactPhone(requestDTO.getContactPhone());

        Partner updatedPartner = partnerRepository.save(partner);
        return convertToDTO(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDTO updatePartnerStatus(UUID partnerId, UserStatusUpdateDTO dto) { // SỬA: Long -> UUID
        logger.info("Admin updating status for partnerId: {} to {}", partnerId, dto.getStatus());

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));

        UserAccountStatus newStatus;
        try {
            newStatus = UserAccountStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + dto.getStatus());
        }

        partner.setStatus(newStatus);
        Partner updatedPartner = partnerRepository.save(partner);

        return convertToDTO(updatedPartner);
    }

    @Override
    @Transactional
    public void deletePartner(UUID partnerId) { // SỬA: Long -> UUID
        if (!partnerRepository.existsById(partnerId)) {
            throw new ResourceNotFoundException("Partner not found with id: " + partnerId);
        }
        partnerRepository.deleteById(partnerId);
        logger.info("Deleted partner with ID: {}", partnerId);
    }

    // Helper method to convert Partner Entity to DTO
    private PartnerResponseDTO convertToDTO(Partner partner) {
        PartnerResponseDTO dto = new PartnerResponseDTO();
        dto.setId(partner.getId());
        dto.setName(partner.getName());
        dto.setOrganizationType(partner.getOrganizationType());
        dto.setContactEmail(partner.getContactEmail());
        dto.setContactPhone(partner.getContactPhone());
        dto.setCreatedAt(partner.getCreatedAt());
        dto.setStatus(partner.getStatus().name());
        if (partner.getWallet() != null) {
            dto.setWalletId(partner.getWallet().getId());
        }
        return dto;
    }
}