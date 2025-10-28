package com.example.demo.service;

import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.response.CheckinResponseDTO; // Cần tạo DTO này

public interface CheckinService {
    
    /**
     * Checks in a student to an event using their phone number and triggers the reward.
     * @param eventId The ID of the event.
     * @param requestDTO DTO containing the student's phone number.
     * @return A DTO of the check-in record, including reward status.
     */
    CheckinResponseDTO performCheckin(Long eventId, CheckinRequestDTO requestDTO);
}