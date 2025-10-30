package com.example.demo.service.impl;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.example.demo.dto.request.TestLoginRequestDTO;
import com.example.demo.dto.response.TestLoginResponseDTO;
import com.example.demo.service.TestLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class TestLoginServiceImpl implements TestLoginService {

    private static final Logger log = LoggerFactory.getLogger(TestLoginServiceImpl.class);

    private final AWSCognitoIdentityProvider cognitoClient;

    @Value("${aws.cognito.userPoolClientId}")
    private String userPoolClientId;

    public TestLoginServiceImpl(AWSCognitoIdentityProvider cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    @Override
    public TestLoginResponseDTO loginForTest(TestLoginRequestDTO request) {
        log.warn("Performing test login for user: {}", request.getEmail());

        // 1. CHUẨN BỊ THAM SỐ
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", request.getEmail());
        authParams.put("PASSWORD", request.getPassword());

        // 2. KHỞI TẠO REQUEST ĐĂNG NHẬP THÔNG THƯỜNG
        InitiateAuthRequest authRequest = new InitiateAuthRequest(); // <<< SỬ DỤNG InitiateAuth
        
        // SỬ DỤNG USER_PASSWORD_AUTH (phải được bật trong App Client)
        authRequest.withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH); 
        
        authRequest.withClientId(userPoolClientId);
        authRequest.withAuthParameters(authParams);

        try {
            // 3. GỌI API COGNITO
            InitiateAuthResult result = cognitoClient.initiateAuth(authRequest); // <<< GỌI initiateAuth

            // 4. KIỂM TRA VÀ TRẢ VỀ KẾT QUẢ
            if (result.getAuthenticationResult() != null) {
                return new TestLoginResponseDTO(
                        result.getAuthenticationResult().getIdToken(),
                        result.getAuthenticationResult().getAccessToken(),
                        result.getAuthenticationResult().getRefreshToken()
                );
            } else {
                // Xử lý các trường hợp cần xác nhận (ví dụ: NEW_PASSWORD_REQUIRED, MFA) nếu có.
                log.error("Cognito auth error: Authentication required next step: {}", result.getChallengeName());
                throw new RuntimeException("Cognito authentication requires next step: " + result.getChallengeName());
            }

        } catch (NotAuthorizedException e) {
            log.error("Cognito auth error: Invalid credentials. {}", e.getErrorMessage());
            throw new RuntimeException("Invalid username or password.", e);
        } catch (InvalidParameterException e) {
            // Bao gồm lỗi "Auth flow not enabled for this client"
            log.error("Cognito auth error: Auth flow not enabled for this client (or other parameter error). {}", e.getErrorMessage());
            throw new RuntimeException("Cognito authentication error (check Cognito configuration).", e);
        } catch (Exception e) {
            log.error("Cognito auth error: {}", e.getMessage());
            throw new RuntimeException("Cognito authentication error.", e);
        }
    }
}