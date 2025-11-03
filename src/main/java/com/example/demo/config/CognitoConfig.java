package com.example.demo.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions; // <<< Import này rất quan trọng
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CognitoConfig {

    // Lấy các giá trị cấu hình từ application.properties/yaml
    @Value("${AWS_REGION}") // Đọc biến AWS_REGION
    private String awsRegion;
    
    @Value("${AWS_ACCESS_KEY_ID}") // Đọc biến AWS_ACCESS_KEY_ID
    private String accessKeyId; 

    @Value("${AWS_SECRET_ACCESS_KEY}") // Đọc biến AWS_SECRET_ACCESS_KEY
    private String secretAccessKey;

    @Bean
    public AWSCognitoIdentityProvider cognitoIdentityProvider() {
        
        // 1. Tạo Credential Provider (nếu bạn dùng Access Key/Secret Key)
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(accessKeyId, secretAccessKey)
        );

        // 2. Tạo Client Cognito
        return AWSCognitoIdentityProviderClientBuilder.standard()
                // Xác định Region (ví dụ: Regions.AP_SOUTHEAST_2)
                .withRegion(Regions.fromName(awsRegion)) 
                // Cung cấp thông tin xác thực
                .withCredentials(credentialsProvider)
                .build();
    }
}