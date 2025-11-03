package com.example.demo.service;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.response.CheckinResponseDTO; // Cần tạo DTO này
import com.example.demo.dto.response.StudentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CheckinService {
    
    /**
     * Checks in a student to an event using their phone number and triggers the reward.
     * @param eventId The ID of the event.
     * @param requestDTO DTO containing the student's phone number.
     * @return A DTO of the check-in record, including reward status.
     */
    CheckinResponseDTO performCheckin(Long eventId, CheckinRequestDTO requestDTO, AuthPrincipal principal);
    CheckinResponseDTO registerEvent(String cognitoSub, Long eventId);
    Page<StudentResponseDTO> getAttendeesByEvent(Long eventId, Pageable pageable);
}