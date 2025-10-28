package com.example.demo.service;

import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO; // Cần tạo DTO

public interface FeedbackService {
    FeedbackResponseDTO createFeedback(String cognitoSub, Long eventId, FeedbackRequestDTO requestDTO);
}