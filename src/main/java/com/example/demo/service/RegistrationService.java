package com.example.demo.service;

import com.example.demo.dto.response.RegistrationResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationService {

    /**
     * Registers an authenticated student for a specific event.
     * @param cognitoSub The 'sub' ID from the student's JWT.
     * @param eventId The ID of the event to register for.
     * @return A DTO of the created registration.
     */
    RegistrationResponseDTO createRegistration(String cognitoSub, Long eventId);

    /**
     * Gets a paginated list of students (attendees) registered for a specific event.
     * @param eventId The ID of the event.
     * @param pageable Pagination info.
     * @return A page of student DTOs.
     */
    Page<StudentResponseDTO> getAttendeesByEvent(Long eventId, Pageable pageable);
}