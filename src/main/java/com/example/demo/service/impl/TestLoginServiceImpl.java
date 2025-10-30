package com.example.demo.service.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.example.demo.dto.request.TestLoginRequestDTO;
import com.example.demo.dto.response.TestLoginResponseDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.TestLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class TestLoginServiceImpl implements TestLoginService {

    private static final Logger logger = LoggerFactory.getLogger(TestLoginServiceImpl.class);

    private final AWSCognitoIdentityProvider cognitoClient;

    @Value("${AWS_COGNITO_USER_POOL_ID}")
    private String userPoolId;

    @Value("${AWS_COGNITO_APPCLIENTID}") // <<< Đảm bảo tên khóa chính xác này
    private String appClientId;

    // Constructor này sao chép y hệt logic từ PartnerServiceImpl (file 88)
    // để khởi tạo Cognito client
    public TestLoginServiceImpl(
            @Value("${AWS_ACCESS_KEY_ID}") String accessKey, // <<< SỬA Ở ĐÂY
            @Value("${AWS_SECRET_ACCESS_KEY}") String secretKey, // <<< SỬA Ở ĐÂY
            @Value("${AWS_REGION}") String awsRegion) {
        
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        this.cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(awsRegion)
                .build();
    }

    @Override
    public TestLoginResponseDTO loginForTest(TestLoginRequestDTO requestDTO) {
        logger.warn("Performing test login for user: {}", requestDTO.getUsername());

        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", requestDTO.getUsername());
        authParams.put("PASSWORD", requestDTO.getPassword());
        
        // Chúng ta dùng ADMIN_NO_SRP_AUTH, một flow admin không cần tính toán SRP
        // Flow này yêu cầu quyền cognito:AdminInitiateAuth
        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withUserPoolId(userPoolId)
                .withClientId(appClientId)
                .withAuthParameters(authParams);

        try {
            AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);
            AuthenticationResultType authResult = result.getAuthenticationResult();

            if (authResult == null || authResult.getAccessToken() == null) {
                throw new BadRequestException("Cognito did not return an access token.");
            }

            logger.warn("Test login successful for user: {}", requestDTO.getUsername());
            return new TestLoginResponseDTO(
                authResult.getAccessToken(),
                authResult.getIdToken()
            );

        } catch (UserNotFoundException e) {
            logger.error("Test login failed: User {} not found", requestDTO.getUsername());
            throw new ResourceNotFoundException("User not found.");
        } catch (NotAuthorizedException e) {
            logger.error("Test login failed: Incorrect username or password for user {}", requestDTO.getUsername());
            throw new BadRequestException("Incorrect username or password.");
        } catch (Exception e) {
            logger.error("Cognito auth error: {}", e.getMessage());
            throw new RuntimeException("Cognito authentication error.", e);
        }
    }
}