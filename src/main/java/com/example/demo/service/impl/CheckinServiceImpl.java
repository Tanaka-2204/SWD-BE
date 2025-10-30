package com.example.demo.service.impl;

import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.response.CheckinResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.CheckinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class CheckinServiceImpl implements CheckinService {

    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final CheckinRepository checkinRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public CheckinServiceImpl(EventRepository eventRepository, StudentRepository studentRepository,
            RegistrationRepository registrationRepository, CheckinRepository checkinRepository,
            WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.checkinRepository = checkinRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public CheckinResponseDTO performCheckin(Long eventId, CheckinRequestDTO requestDTO) {
        // 1. Tìm sự kiện
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // 2. Tìm sinh viên
        Student student = studentRepository.findByPhoneNumber(requestDTO.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with phone: " + requestDTO.getPhoneNumber()));

        // 3. Kiểm tra đăng ký (Đúng)
        registrationRepository.findByStudentIdAndEventId(student.getId(), eventId)
                .orElseThrow(() -> new DataIntegrityViolationException("Student is not registered for this event."));

        // 4. Kiểm tra đã check-in (Đúng)
        checkinRepository.findByEventIdAndStudentId(eventId, student.getId()).ifPresent(c -> {
            throw new DataIntegrityViolationException("Student has already checked in.");
        });

        // 5. Tạo bản ghi Check-in
        Checkin checkin = new Checkin();
        checkin.setEvent(event);
        checkin.setStudent(student);
        checkin.setPhoneNumber(requestDTO.getPhoneNumber());
        checkin.setCheckinTime(OffsetDateTime.now());

        // Đánh dấu check-in thành công
        checkin.setVerified(true);
        Checkin savedCheckin = checkinRepository.save(checkin);
        Integer depositPoints = event.getPointCostToRegister();

        return new CheckinResponseDTO(
                savedCheckin.getId(),
                student.getFullName(),
                event.getTitle(),
                false, 
                new BigDecimal(depositPoints) 
        );
    }
}