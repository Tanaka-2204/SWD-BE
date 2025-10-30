package com.example.demo.service.impl;

import com.example.demo.dto.request.FeedbackRequestDTO;
import com.example.demo.dto.response.FeedbackResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, StudentRepository studentRepository,
            EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(Long studentId, Long eventId, FeedbackRequestDTO requestDTO) {

        // 1. Tìm sinh viên (Bây giờ dùng ID nội bộ, không cần sub)
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found.")); // Lỗi này không nên xảy ra nếu
                                                                                         // logic security đúng

        // 2. Tìm sự kiện (Giữ nguyên)
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // 3. (Quan trọng) Kiểm tra xem sinh viên đã tham gia sự kiện này chưa (Giữ
        // nguyên)
        registrationRepository.findByStudentIdAndEventId(student.getId(), eventId)
                .orElseThrow(() -> new ForbiddenException("You did not register for this event."));

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

        // TODO: Phân tích cảm xúc (sentiment analysis) cho comments nếu cần
        // feedback.setSentimentLabel(analyzeSentiment(requestDTO.getComments()));

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return convertToDTO(savedFeedback);
    }

    // Helper
    private FeedbackResponseDTO convertToDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        dto.setRating(feedback.getRating());
        dto.setComments(feedback.getComments());
        dto.setSentimentLabel(feedback.getSentimentLabel());
        dto.setCreatedAt(feedback.getCreatedAt());

        if (feedback.getStudent() != null) {
            dto.setStudentId(feedback.getStudent().getId());
        }
        if (feedback.getEvent() != null) {
            dto.setEventId(feedback.getEvent().getId());
            dto.setEventTitle(feedback.getEvent().getTitle());
        }
        return dto;
    }
}