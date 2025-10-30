package com.example.demo.service.impl;

import com.example.demo.dto.request.PartnerRequestDTO;
import com.example.demo.dto.response.PartnerResponseDTO;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Wallet;
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
import com.amazonaws.services.cognitoidp.model.AliasExistsException;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.example.demo.exception.BadRequestException;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Value; 

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartnerServiceImpl implements PartnerService {

    private static final Logger logger = LoggerFactory.getLogger(PartnerServiceImpl.class); // Thêm logger

    private final PartnerRepository partnerRepository;
    private final WalletRepository walletRepository;
    private final AWSCognitoIdentityProvider cognitoClient; // <<< GIỮ LẠI KHAI BÁO NÀY

    @Value("${AWS_COGNITO_USER_POOL_ID}") // <<< GIỮ LẠI KHAI BÁO NÀY
    private String userPoolId;

    // <<< GIỮ LẠI CONSTRUCTOR NÀY (Đã sửa lỗi) >>>
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
        try {
            AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId)
                    // ===>>> SỬ DỤNG USERNAME TỪ DTO <<<===
                    .withUsername(partnerCognitoUsername) 
                    .withUserAttributes(
                            new AttributeType().withName("email").withValue(partnerEmail),
                            new AttributeType().withName("name").withValue(requestDTO.getName()),
                            new AttributeType().withName("email_verified").withValue("true"),
                            // ===>>> THÊM CUSTOM ATTRIBUTE <<<===
                            new AttributeType().withName("custom:user_type").withValue("PARTNER") 
                    )
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .withForceAliasCreation(Boolean.FALSE);

            AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(createUserRequest);
            createdCognitoSub = createUserResult.getUser().getAttributes().stream()
                                        .filter(attr -> "sub".equals(attr.getName()))
                                        .map(AttributeType::getValue)
                                        .findFirst().orElse(null);
            logger.info("Successfully created Cognito user: {} with sub: {}", partnerCognitoUsername, createdCognitoSub);

            // Thêm user vào group PARTNERS (vẫn dùng username từ DTO)
            AdminAddUserToGroupRequest addUserToGroupRequest = new AdminAddUserToGroupRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(partnerCognitoUsername) // Dùng username từ DTO
                    .withGroupName("PARTNERS");

            cognitoClient.adminAddUserToGroup(addUserToGroupRequest);
            logger.info("Successfully added Cognito user {} to group PARTNERS", partnerCognitoUsername);

        } catch (UsernameExistsException e) {
             logger.warn("Cognito username {} already exists. Attempting to add to group PARTNERS.", partnerCognitoUsername);
             // Xử lý logic add group cho user đã tồn tại (giữ nguyên)
             try {
                 AdminAddUserToGroupRequest addUserToGroupRequest = new AdminAddUserToGroupRequest()
                         .withUserPoolId(userPoolId)
                         .withUsername(partnerCognitoUsername)
                         .withGroupName("PARTNERS");
                 cognitoClient.adminAddUserToGroup(addUserToGroupRequest);
                 logger.info("Successfully added existing Cognito user {} to group PARTNERS", partnerCognitoUsername);
             } catch (Exception addGroupError) {
                  logger.error("Failed to add existing Cognito user {} to group PARTNERS: {}", partnerCognitoUsername, addGroupError.getMessage());
                  throw new RuntimeException("Failed to assign partner role in Cognito for existing user.", addGroupError);
             }
        } catch (AliasExistsException e) {
             logger.error("Cognito user with email alias {} already exists.", partnerEmail, e);
             throw new DataIntegrityViolationException("Partner email already exists in Cognito.", e);
        } catch (InvalidParameterException e) {
             logger.error("Invalid parameter creating Cognito user {}: {}", partnerCognitoUsername, e.getMessage());
             throw new BadRequestException("Invalid username or attributes for Cognito: " + e.getMessage(), e); 
        }
        catch (Exception e) {
            logger.error("Failed to create Cognito user {} or add to group: {}", partnerCognitoUsername, e.getMessage(), e);
            throw new RuntimeException("Failed to create Cognito user or assign role. Partner creation rolled back.", e);
        }
        // ================================================

        // 3. Tạo Wallet và Partner trong Database
        Partner partner = new Partner();
        partner.setName(requestDTO.getName());
        partner.setOrganizationType(requestDTO.getOrganizationType());
        partner.setContactEmail(requestDTO.getContactEmail());
        partner.setContactPhone(requestDTO.getContactPhone());
        partner.setCognitoSub(createdCognitoSub); // Lưu sub ID

        // ... (Code tạo Wallet, lưu Partner, cập nhật Wallet Owner ID giữ nguyên) ...
        Wallet wallet = new Wallet();
        wallet.setOwnerType("PARTNER");
        wallet.setCurrency("COIN");
        wallet.setBalance(BigDecimal.ZERO);
        Wallet savedWallet = walletRepository.save(wallet);

        partner.setWallet(savedWallet);
        Partner savedPartner = partnerRepository.save(partner);

        savedWallet.setOwnerId(savedPartner.getId());
        walletRepository.save(savedWallet);

        logger.info("Successfully created partner {} with ID {} in database", savedPartner.getName(), savedPartner.getId());
        return convertToDTO(savedPartner);
    }

    // --- Các phương thức GET, UPDATE, DELETE giữ nguyên ---
    @Override
    @Transactional(readOnly = true)
    public PartnerResponseDTO getPartnerById(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + partnerId));
        return convertToDTO(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerResponseDTO> getAllPartners() {
        return partnerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PartnerResponseDTO updatePartner(Long partnerId, PartnerRequestDTO requestDTO) {
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
    public void deletePartner(Long partnerId) {
        if (!partnerRepository.existsById(partnerId)) {
            throw new ResourceNotFoundException("Partner not found with id: " + partnerId);
        }
        // TODO: Thêm logic kiểm tra ràng buộc trước khi xóa (ví dụ: còn sự kiện nào
        // không?)
        // TODO: Cân nhắc việc xóa user Cognito tương ứng
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
        if (partner.getWallet() != null) {
            dto.setWalletId(partner.getWallet().getId());
        }
        return dto;
    }
}