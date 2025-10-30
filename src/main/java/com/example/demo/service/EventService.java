package com.example.demo.service;

import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Thêm
import com.example.demo.entity.Event;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;

public interface EventService {

    // --- CRUD Methods ---
    EventResponseDTO createEvent(Jwt jwt, EventCreateDTO requestDTO);

    EventResponseDTO getEventById(Long eventId);

    Page<EventResponseDTO> getAllEvents(Specification<Event> spec, Pageable pageable);

    EventResponseDTO updateEvent(Long eventId, EventUpdateDTO requestDTO);

    void deleteEvent(Long eventId);
    
    // --- Business Logic Methods ---
    Page<EventResponseDTO> getEventsByPartner(Long partnerId, Pageable pageable);

    List<EventResponseDTO> getEventsByCategory(Long categoryId);

    Page<EventResponseDTO> getUpcomingEvents(Pageable pageable);
    
    List<EventResponseDTO> getOngoingEvents();

    Page<EventResponseDTO> searchEventsByTitle(String keyword, Pageable pageable);
}