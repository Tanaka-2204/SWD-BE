package com.example.demo.service.impl;

import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;
import com.example.demo.entity.BroadcastDelivery;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventBroadcast;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BroadcastDeliveryRepository;
import com.example.demo.repository.EventBroadcastRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.CheckinRepository; 
import java.util.UUID;
import com.example.demo.service.BroadcastService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BroadcastServiceImpl implements BroadcastService {

    private final PartnerRepository partnerRepository;
    private final EventRepository eventRepository;
    private final CheckinRepository checkinRepository; 
    private final EventBroadcastRepository eventBroadcastRepository;
    private final BroadcastDeliveryRepository broadcastDeliveryRepository;

    public BroadcastServiceImpl(PartnerRepository partnerRepository, 
                                EventRepository eventRepository, 
                                CheckinRepository checkinRepository, 
                                EventBroadcastRepository eventBroadcastRepository, 
                                BroadcastDeliveryRepository broadcastDeliveryRepository) {
        this.partnerRepository = partnerRepository;
        this.eventRepository = eventRepository;
        this.checkinRepository = checkinRepository; 
        this.eventBroadcastRepository = eventBroadcastRepository;
        this.broadcastDeliveryRepository = broadcastDeliveryRepository;
    }

    @Override
    @Transactional
    public EventBroadcastResponseDTO sendBroadcast(UUID partnerId, BroadcastRequestDTO requestDTO) {
        // 1. Lấy sự kiện
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + requestDTO.getEventId()));
        
        // 2. Kiểm tra quyền: Partner này có phải người tổ chức sự kiện không?
        if (!event.getPartner().getId().equals(partnerId)) {
            throw new ForbiddenException("Partner does not own this event.");
        }

        // 3. Tạo bản ghi broadcast chính
        EventBroadcast broadcast = new EventBroadcast();
        broadcast.setEvent(event);
        broadcast.setMessageContent(requestDTO.getMessageContent());
        EventBroadcast savedBroadcast = eventBroadcastRepository.save(broadcast);

        // 4. Lấy danh sách sinh viên đã đăng ký (TỪ BẢNG CHECKIN)
        List<Checkin> checkins = checkinRepository.findAllByEventId(event.getId());

        // 5. Tạo các bản ghi delivery cho từng sinh viên
        if (!checkins.isEmpty()) {
            List<BroadcastDelivery> deliveries = checkins.stream()
                    .map(checkin -> { 
                        BroadcastDelivery delivery = new BroadcastDelivery();
                        delivery.setBroadcast(savedBroadcast);
                        delivery.setStudent(checkin.getStudent()); 
                        delivery.setStatus("SENT");
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
        // 1. Tạo bản ghi broadcast chính (không liên kết với event cụ thể)
        EventBroadcast broadcast = new EventBroadcast();
        broadcast.setMessageContent(requestDTO.getMessageContent());
        EventBroadcast savedBroadcast = eventBroadcastRepository.save(broadcast);
        
        return convertToDTO(savedBroadcast);
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