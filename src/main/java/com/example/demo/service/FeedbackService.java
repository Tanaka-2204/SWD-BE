package com.example.demo.service;

import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.response.FeedbackResponseDTO;
import org.springframework.data.domain.Page;      
import org.springframework.data.domain.Pageable;     
import java.util.UUID;

public interface FeedbackService {

    FeedbackResponseDTO createFeedback(UUID studentId, UUID eventId, FeedbackRequestDTO requestDTO);

    Page<FeedbackResponseDTO> getAllFeedbackByEvent(UUID eventId, Pageable pageable);

    Page<FeedbackResponseDTO> getAllFeedback(UUID eventId, Pageable pageable);

    FeedbackResponseDTO updateFeedback(UUID feedbackId, FeedbackRequestDTO requestDTO, AuthPrincipal principal);

    void deleteFeedback(UUID feedbackId, AuthPrincipal principal);

    Page<FeedbackResponseDTO> getMyFeedback(AuthPrincipal principal, Pageable pageable);
}