package com.example.demo.service;

import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import org.springframework.data.domain.Page;         // <<< THÊM
import org.springframework.data.domain.Pageable;      // <<< THÊM
import java.util.UUID;

public interface FeedbackService {

    FeedbackResponseDTO createFeedback(UUID studentId, UUID eventId, FeedbackRequestDTO requestDTO);

    /**
     * Lấy tất cả feedback (phân trang) cho một sự kiện cụ thể.
     */
    Page<FeedbackResponseDTO> getAllFeedbackByEvent(UUID eventId, Pageable pageable); // <<< THÊM

    /**
     * Lấy tất cả feedback (phân trang) trên toàn hệ thống, có thể lọc theo eventId.
     */
    Page<FeedbackResponseDTO> getAllFeedback(UUID eventId, Pageable pageable); // <<< THÊM
}