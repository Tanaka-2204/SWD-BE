package com.example.demo.service;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.EventResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.demo.entity.Event;
import java.util.List;
import java.util.UUID;

public interface EventService {

    // --- CRUD Methods ---
    EventResponseDTO createEvent(AuthPrincipal principal, EventCreateDTO requestDTO);

    EventResponseDTO getEventById(UUID eventId);

    Page<EventResponseDTO> getAllEvents(Specification<Event> spec, Pageable pageable);

    EventResponseDTO updateEvent(UUID eventId, EventUpdateDTO requestDTO, AuthPrincipal principal);

    void deleteEvent(UUID eventId, AuthPrincipal principal);
    
    // --- Business Logic Methods ---
    Page<EventResponseDTO> getEventsByPartner(UUID partnerId, Pageable pageable);

    List<EventResponseDTO> getEventsByCategory(UUID categoryId);

    Page<EventResponseDTO> getUpcomingEvents(Pageable pageable);

    Page<EventResponseDTO> searchEventsByTitle(String keyword, Pageable pageable);
    
    Page<EventResponseDTO> getEventHistoryByStudent(UUID studentId, Pageable pageable);

    // ==============================================================
    // PHƯƠNG THỨC MỚI: Hoàn tất và thanh toán điểm cho người tham dự
    // ==============================================================
    /**
     * Xử lý việc hoàn tất sự kiện: trả lại điểm cọc và cộng điểm thưởng cho
     * tất cả sinh viên đã check-in thành công.
     * @param eventId ID của sự kiện cần hoàn tất
     * @return EventResponseDTO của sự kiện đã được cập nhật trạng thái
     */
    EventResponseDTO finalizeEvent(UUID eventId);

    // Admin approve event
    EventResponseDTO approveEvent(UUID eventId);
    EventResponseDTO finalizeEvent(UUID eventId, AuthPrincipal principal);
}