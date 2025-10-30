package com.example.demo.service.impl;

import com.example.demo.dto.response.RegistrationResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO; // Cần import
import com.example.demo.entity.Event;
import com.example.demo.entity.Registration;
import com.example.demo.entity.Student;
import com.example.demo.exception.DataIntegrityViolationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.RegistrationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final StudentServiceImpl studentServiceHelper; // Để dùng hàm convertToDTO

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   StudentRepository studentRepository,
                                   EventRepository eventRepository,
                                   StudentServiceImpl studentServiceHelper) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.studentServiceHelper = studentServiceHelper;
    }

    @Override
    @Transactional
    public RegistrationResponseDTO createRegistration(Long studentId, Long eventId) { 
        // 1. Tìm sinh viên (Bây giờ dùng ID nội bộ)
        Student student = studentRepository.findById(studentId) // <<< SỬA Ở ĐÂY
                .orElseThrow(() -> new ResourceNotFoundException("Student not found.")); // Lỗi này không nên xảy ra

        // 2. Tìm sự kiện (Giữ nguyên)
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // 3. Kiểm tra đã đăng ký chưa (Giữ nguyên)
        registrationRepository.findByStudentIdAndEventId(student.getId(), eventId).ifPresent(reg -> {
            throw new DataIntegrityViolationException("Student is already registered for this event.");
        });

        // 4. Tạo đăng ký (Giữ nguyên)
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setEvent(event);
        
        Registration savedReg = registrationRepository.save(registration);
        return convertToDTO(savedReg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDTO> getAttendeesByEvent(Long eventId, Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        
        Page<Registration> registrations = registrationRepository.findAllByEventId(eventId, pageable);
        // Dùng hàm map của Page để chuyển đổi Registration -> Student -> StudentResponseDTO
        return registrations.map(reg -> studentServiceHelper.toResponseDTO(reg.getStudent()));
    }
    
    // Helper DTO
    private RegistrationResponseDTO convertToDTO(Registration reg) {
        RegistrationResponseDTO dto = new RegistrationResponseDTO();
        dto.setId(reg.getId());
        dto.setRegisteredAt(reg.getRegisteredAt());
        if(reg.getStudent() != null) {
            dto.setStudentId(reg.getStudent().getId());
            dto.setStudentName(reg.getStudent().getFullName());
        }
        if(reg.getEvent() != null) {
            dto.setEventId(reg.getEvent().getId());
            dto.setEventTitle(reg.getEvent().getTitle());
        }
        return dto;
    }
}