package com.example.demo.lambda.dto; // Hoặc package phù hợp

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the 'request' part of the Cognito event trigger payload.
 */
@Data
@NoArgsConstructor
public class CognitoEventRequest {
    private CognitoUserAttributes userAttributes;
}