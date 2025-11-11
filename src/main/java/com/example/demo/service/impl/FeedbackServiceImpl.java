package com.example.demo.service.impl;

import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional; 
import java.util.UUID; // <<< THÊM IMPORT

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final CheckinRepository checkinRepository; 

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, StudentRepository studentRepository,
                               EventRepository eventRepository, CheckinRepository checkinRepository) {
        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.checkinRepository = checkinRepository; 
    }

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(UUID studentId, UUID eventId, FeedbackRequestDTO requestDTO) { // SỬA: Long -> UUID

        // 1. Tìm sinh viên (Giữ nguyên)
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found.")); 

        // 2. Tìm sự kiện (Giữ nguyên)
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // 3. (Quan trọng) Kiểm tra xem sinh viên đã đăng ký sự kiện này chưa
        boolean isRegistered = checkinRepository.existsByEventIdAndStudentId(eventId, student.getId());
        if (!isRegistered) {
             throw new ForbiddenException("You did not register for this event.");
        }

        // 4. Kiểm tra xem đã feedback chưa (Giữ nguyên)
        feedbackRepository.findByStudentIdAndEventId(student.getId(), eventId).ifPresent(f -> {
            throw new DataIntegrityViolationException("You have already submitted feedback for this event.");
        });

        // 5. Tạo feedback (Giữ nguyên)
        Feedback feedback = new Feedback();
        feedback.setStudent(student);
        feedback.setEvent(event);
        feedback.setRating(requestDTO.getRating());
        feedback.setComments(requestDTO.getComments());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return convertToDTO(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponseDTO> getAllFeedbackByEvent(UUID eventId, Pageable pageable) { // SỬA: Long -> UUID
        Page<Feedback> feedbackPage = feedbackRepository.findByEventId(eventId, pageable);
        return feedbackPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponseDTO> getAllFeedback(UUID eventId, Pageable pageable) { // SỬA: Long -> UUID
        Page<Feedback> feedbackPage;
        
        if (eventId != null) {
            feedbackPage = feedbackRepository.findByEventId(eventId, pageable);
        } else {
            feedbackPage = feedbackRepository.findAll(pageable);
        }
        
        return feedbackPage.map(this::convertToDTO);
    }

    // Helper (Giữ nguyên)
    private FeedbackResponseDTO convertToDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        dto.setRating(feedback.getRating());
        dto.setComments(feedback.getComments());
        dto.setSentimentLabel(feedback.getSentimentLabel());
        dto.setCreatedAt(feedback.getCreatedAt());

        if (feedback.getStudent() != null) {
            dto.setStudentId(feedback.getStudent().getId());
            dto.setStudentName(feedback.getStudent().getFullName()); 
        }
        if (feedback.getEvent() != null) {
            dto.setEventId(feedback.getEvent().getId());
            dto.setEventTitle(feedback.getEvent().getTitle()); 
        }
        return dto;
    }
}