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

public interface EventService {

    // --- CRUD Methods ---
    EventResponseDTO createEvent(AuthPrincipal principal, EventCreateDTO requestDTO);

    EventResponseDTO getEventById(Long eventId);

    Page<EventResponseDTO> getAllEvents(Specification<Event> spec, Pageable pageable);

    EventResponseDTO updateEvent(Long eventId, EventUpdateDTO requestDTO, AuthPrincipal principal);

    void deleteEvent(Long eventId, AuthPrincipal principal);
    
    // --- Business Logic Methods ---
    Page<EventResponseDTO> getEventsByPartner(Long partnerId, Pageable pageable);

    List<EventResponseDTO> getEventsByCategory(Long categoryId);

    Page<EventResponseDTO> getUpcomingEvents(Pageable pageable);

    Page<EventResponseDTO> searchEventsByTitle(String keyword, Pageable pageable);

    Page<EventResponseDTO> getEventHistoryByStudent(Long studentId, Pageable pageable);

    // ==============================================================
    // PHƯƠNG THỨC MỚI: Hoàn tất và thanh toán điểm cho người tham dự
    // ==============================================================
    /**
     * Xử lý việc hoàn tất sự kiện: trả lại điểm cọc và cộng điểm thưởng cho
     * tất cả sinh viên đã check-in thành công.
     * @param eventId ID của sự kiện cần hoàn tất
     * @return EventResponseDTO của sự kiện đã được cập nhật trạng thái
     */
    EventResponseDTO finalizeEvent(Long eventId);

    // Admin approve event
    EventResponseDTO approveEvent(Long eventId);
    EventResponseDTO finalizeEvent(Long eventId, AuthPrincipal principal);
}