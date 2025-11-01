package com.example.demo.lambda; // Hoặc package phù hợp

import com.example.demo.lambda.dto.CognitoEvent;
// import com.example.demo.repository.StudentRepository; // <<< KHÔNG CẦN
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class SyncCognitoUserFunction {

    private static final Logger logger = LoggerFactory.getLogger(SyncCognitoUserFunction.class);

    /**
     * Bean Lambda này được gọi bởi Cognito Post Confirmation.
     * Nó CHỈ LOG LẠI sự kiện, KHÔNG đồng bộ vào DB.
     * Việc tạo Student sẽ do API /complete-profile đảm nhiệm.
     */
    @Bean
    public Function<CognitoEvent, String> syncUser() { // <<< XÓA StudentRepository
        return event -> {
            
            if (event == null || event.getRequest() == null || event.getRequest().getUserAttributes() == null) {
                logger.warn("Cognito trigger: Invalid event structure.");
                return "SKIPPED: Invalid event";
            }
            
            String cognitoSub = event.getRequest().getUserAttributes().getSub();
            String email = event.getRequest().getUserAttributes().getEmail();
            
            logger.info("Cognito trigger: Received Post Confirmation for user sub={}, email={}. " +
                        "Skipping DB sync. Profile will be created via API.", 
                        cognitoSub, email);

            // Chỉ log và trả về thành công, không làm gì cả
            return "SUCCESS: Skipped (Handled by API)";
        };
    }
}