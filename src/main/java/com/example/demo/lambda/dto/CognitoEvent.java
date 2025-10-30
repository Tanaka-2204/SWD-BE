package com.example.demo.lambda.dto; // Hoặc package phù hợp

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the overall event structure received from a Cognito User Pool trigger.
 * Adjust fields based on the specific trigger type (e.g., Post Confirmation).
 */
@Data
@NoArgsConstructor
public class CognitoEvent {
    private String version;
    private String region;
    private String userPoolId;
    private String userName; // The username the user signed up with
    private String triggerSource; // e.g., "PostConfirmation_ConfirmSignUp"
    private CognitoEventRequest request;
    private CognitoEventResponse response; // Usually empty for Post Confirmation

    // Inner class for response if needed by other triggers
    @Data
    @NoArgsConstructor
    public static class CognitoEventResponse {
        // Response fields if applicable (e.g., for pre-signup trigger)
    }
}