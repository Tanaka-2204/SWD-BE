package com.example.demo.lambda; // Hoặc package phù hợp

import com.example.demo.lambda.dto.CognitoEvent;
import com.example.demo.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * Spring Cloud Function configuration containing the Bean that handles
 * Cognito trigger events (e.g., Post Confirmation) to sync user data.
 */
@Configuration
public class SyncCognitoUserFunction {

    private static final Logger logger = LoggerFactory.getLogger(SyncCognitoUserFunction.class);

    /**
     * The main Lambda function handler bean.
     * Receives a CognitoEvent, extracts user attributes, and performs an upsert
     * operation (Insert or Update) into the local 'student' table.
     *
     * @param studentRepository The repository to interact with the student table.
     * @return A Function that processes the Cognito event.
     */
    @Bean
    public Function<CognitoEvent, String> syncUser(StudentRepository studentRepository) {
        return event -> {
            // Basic validation of the incoming event structure
            if (event == null || event.getRequest() == null || event.getRequest().getUserAttributes() == null) {
                logger.error("Received invalid or incomplete Cognito event structure: {}", event);
                // Return an error message, but don't throw an exception here
                // as Cognito might retry indefinitely for certain errors.
                // Log the error for monitoring.
                return "ERROR: Invalid event data received.";
            }

            String cognitoSub = event.getRequest().getUserAttributes().getSub();
            String email = event.getRequest().getUserAttributes().getEmail();
            // Adjust attribute name if needed (e.g., event.getRequest().getUserAttributes().getPreferred_username())
            String name = event.getRequest().getUserAttributes().getName(); 

            // Validate essential attributes
            if (cognitoSub == null || email == null || name == null || cognitoSub.isBlank() || email.isBlank() || name.isBlank()) {
                logger.error("Missing essential user attributes in Cognito event: sub={}, email={}, name={}",
                        cognitoSub, email, name);
                return "ERROR: Missing essential user attributes.";
            }

            try {
                logger.info("Attempting to sync user from Cognito Post Confirmation: sub={}, email={}", cognitoSub, email);

                // Perform the INSERT or UPDATE operation using the native query
                // The @Transactional annotation on the repository method handles the transaction
                studentRepository.upsertStudent(cognitoSub, email, name);

                logger.info("Successfully synced user from Cognito: sub={}", cognitoSub);
                // According to AWS docs for some triggers, returning the input event is expected on success.
                // However, returning a simple success message is often sufficient for Post Confirmation.
                return "SUCCESS";
            } catch (Exception e) {
                // Log the detailed error for debugging
                logger.error("Error syncing user with sub {} from Cognito: {}", cognitoSub, e.getMessage(), e);
                // Throwing an exception tells Cognito the trigger failed, potentially causing retries
                // or user confirmation failure depending on the trigger type and configuration.
                // Be cautious about infinite retries if the error is persistent (e.g., DB connection issue).
                throw new RuntimeException("Failed to sync user to database for sub: " + cognitoSub, e);
            }
        };
    }
}