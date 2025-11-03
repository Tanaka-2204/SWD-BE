package com.example.demo.service;

import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import org.springframework.data.domain.Page;         // <<< THÊM
import org.springframework.data.domain.Pageable;      // <<< THÊM

public interface FeedbackService {

    FeedbackResponseDTO createFeedback(Long studentId, Long eventId, FeedbackRequestDTO requestDTO);

    /**
     * Lấy tất cả feedback (phân trang) cho một sự kiện cụ thể.
     */
    Page<FeedbackResponseDTO> getAllFeedbackByEvent(Long eventId, Pageable pageable); // <<< THÊM

    /**
     * Lấy tất cả feedback (phân trang) trên toàn hệ thống, có thể lọc theo eventId.
     */
    Page<FeedbackResponseDTO> getAllFeedback(Long eventId, Pageable pageable); // <<< THÊM
}