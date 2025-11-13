package com.example.demo.service.impl;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;
import com.example.demo.dto.response.StudentBroadcastResponseDTO;
import com.example.demo.entity.enums.BroadcastDeliveryStatus;
import com.example.demo.exception.BadRequestException;
import com.example.demo.entity.BroadcastDelivery;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventBroadcast;
import com.example.demo.entity.Student;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BroadcastDeliveryRepository;
import com.example.demo.repository.EventBroadcastRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.CheckinRepository;
import java.util.UUID;
import com.example.demo.service.BroadcastService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class BroadcastServiceImpl implements BroadcastService {

    private final PartnerRepository partnerRepository;
    private final EventRepository eventRepository;
    private final CheckinRepository checkinRepository;
    private final EventBroadcastRepository eventBroadcastRepository;
    private final BroadcastDeliveryRepository broadcastDeliveryRepository;
    private final StudentRepository studentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(BroadcastServiceImpl.class);

    public BroadcastServiceImpl(PartnerRepository partnerRepository,
            EventRepository eventRepository,
            CheckinRepository checkinRepository,
            EventBroadcastRepository eventBroadcastRepository,
            BroadcastDeliveryRepository broadcastDeliveryRepository, StudentRepository studentRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.partnerRepository = partnerRepository;
        this.eventRepository = eventRepository;
        this.checkinRepository = checkinRepository;
        this.eventBroadcastRepository = eventBroadcastRepository;
        this.broadcastDeliveryRepository = broadcastDeliveryRepository;
        this.studentRepository = studentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public EventBroadcastResponseDTO sendBroadcast(UUID partnerId, BroadcastRequestDTO requestDTO) {

        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + requestDTO.getEventId()));
        if (!event.getPartner().getId().equals(partnerId)) {
            throw new ForbiddenException("Partner does not own this event.");
        }
        EventBroadcast broadcast = new EventBroadcast();
        broadcast.setEvent(event);
        broadcast.setMessageContent(requestDTO.getMessageContent());
        broadcast.setSentAt(OffsetDateTime.now());
        EventBroadcast savedBroadcast = eventBroadcastRepository.save(broadcast);
        List<Checkin> checkins = checkinRepository.findAllByEventId(event.getId());
        if (!checkins.isEmpty()) {
            List<BroadcastDelivery> deliveries = checkins.stream()
                    .map(checkin -> {
                        BroadcastDelivery delivery = new BroadcastDelivery();
                        delivery.setBroadcast(savedBroadcast);
                        delivery.setStudent(checkin.getStudent());
                        delivery.setStatus(BroadcastDeliveryStatus.UNREAD);
                        Student student = checkin.getStudent();
                        if (student != null && student.getCognitoSub() != null) {
                            StudentBroadcastResponseDTO payload = convertToStudentBroadcastDTO(delivery);
                            messagingTemplate.convertAndSendToUser(
                                    student.getCognitoSub(),
                                    "/queue/notifications",
                                    payload);
                            logger.info("Pushed WebSocket notification to user {}", student.getCognitoSub());
                        }
                        return delivery;
                    })
                    .collect(Collectors.toList());
            broadcastDeliveryRepository.saveAll(deliveries);
        }
        return convertToDTO(savedBroadcast);
    }

    @Override
    @Transactional
    public EventBroadcastResponseDTO sendSystemBroadcast(BroadcastRequestDTO requestDTO) {
        EventBroadcast broadcast = new EventBroadcast();
        broadcast.setMessageContent(requestDTO.getMessageContent());
        EventBroadcast savedBroadcast = eventBroadcastRepository.save(broadcast);

        return convertToDTO(savedBroadcast);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentBroadcastResponseDTO> getMyBroadcasts(AuthPrincipal principal, String status,
            Pageable pageable) {
        UUID studentId = getStudentIdFromPrincipal(principal);

        Page<BroadcastDelivery> deliveryPage;
        if (status != null && !status.isBlank()) {
            BroadcastDeliveryStatus statusEnum;
            try {
                statusEnum = BroadcastDeliveryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Trả về lỗi nếu client gửi status linh tinh (ví dụ: "ABC")
                throw new BadRequestException("Invalid status value: " + status);
            }
            // SỬA: Gọi hàm repository với Enum
            deliveryPage = broadcastDeliveryRepository.findByStudentIdAndStatus(studentId, statusEnum, pageable);
        } else {
            deliveryPage = broadcastDeliveryRepository.findByStudentId(studentId, pageable);
        }

        return deliveryPage.map(this::convertToStudentBroadcastDTO);
    }

    @Override
    @Transactional
    public StudentBroadcastResponseDTO markBroadcastAsRead(AuthPrincipal principal, UUID deliveryId) {
        UUID studentId = getStudentIdFromPrincipal(principal);

        BroadcastDelivery delivery = broadcastDeliveryRepository.findByIdAndStudentId(deliveryId, studentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Broadcast message not found or does not belong to user."));
        if (delivery.getStatus() != BroadcastDeliveryStatus.READ) {
            delivery.setStatus(BroadcastDeliveryStatus.READ);
            delivery = broadcastDeliveryRepository.save(delivery);
        }

        return convertToStudentBroadcastDTO(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadBroadcastCount(AuthPrincipal principal) {
        UUID studentId = getStudentIdFromPrincipal(principal);
        long count = broadcastDeliveryRepository.countByStudentIdAndStatus(studentId, BroadcastDeliveryStatus.UNREAD);
        return Map.of("count", count);
    }

    private UUID getStudentIdFromPrincipal(AuthPrincipal principal) {
        if (!principal.isStudent()) {
            throw new ForbiddenException("Only students can access this resource.");
        }
        UUID studentId = principal.getStudentId();
        if (studentId == null) {
            throw new ResourceNotFoundException("Student profile not found. Please complete your profile.");
        }
        return studentId;
    }

    private StudentBroadcastResponseDTO convertToStudentBroadcastDTO(BroadcastDelivery delivery) {
        StudentBroadcastResponseDTO dto = new StudentBroadcastResponseDTO();
        dto.setDeliveryId(delivery.getId());
        dto.setStatus(delivery.getStatus().name());
        EventBroadcast broadcast = delivery.getBroadcast();
        if (broadcast != null) {
            dto.setMessageContent(broadcast.getMessageContent());
            dto.setSentAt(broadcast.getSentAt());
            if (broadcast.getEvent() != null) {
                dto.setEventId(broadcast.getEvent().getId());
                dto.setEventTitle(broadcast.getEvent().getTitle());
            }
        }
        return dto;
    }

    // Helper method đã được hoàn thiện
    private EventBroadcastResponseDTO convertToDTO(EventBroadcast broadcast) {
        EventBroadcastResponseDTO dto = new EventBroadcastResponseDTO();
        dto.setId(broadcast.getId());
        dto.setMessageContent(broadcast.getMessageContent());
        dto.setSentAt(broadcast.getSentAt());

        if (broadcast.getEvent() != null) {
            dto.setEventId(broadcast.getEvent().getId());
            dto.setEventTitle(broadcast.getEvent().getTitle());
        }

        return dto;
    }
}