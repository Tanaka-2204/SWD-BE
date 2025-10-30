package com.example.demo.lambda.dto; // Hoặc package phù hợp

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents user attributes received from a Cognito event trigger.
 */
@Data
@NoArgsConstructor
public class CognitoUserAttributes {
    private String sub; // The unique user identifier
    private String email;
    private String name; // Check your Cognito attributes if you use given_name/family_name
    private Boolean email_verified; // Example: if you need verification status
    // Add other attributes you need to sync (e.g., phone_number if available)
}