package com.example.demo.service.impl;

import com.example.demo.dto.request.BroadcastRequestDTO;
import com.example.demo.dto.response.EventBroadcastResponseDTO;
import com.example.demo.entity.BroadcastDelivery;
import com.example.demo.entity.Checkin; // <<< SỬA ĐỔI: Import Checkin
import com.example.demo.entity.Event;
import com.example.demo.entity.EventBroadcast;
// import com.example.demo.entity.Registration; // <<< XÓA
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BroadcastDeliveryRepository;
import com.example.demo.repository.EventBroadcastRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.CheckinRepository; // <<< SỬA ĐỔI: Dùng CheckinRepository
// import com.example.demo.repository.RegistrationRepository; // <<< XÓA
import com.example.demo.service.BroadcastService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BroadcastServiceImpl implements BroadcastService {

    private final PartnerRepository partnerRepository;
    private final EventRepository eventRepository;
    private final CheckinRepository checkinRepository; // <<< SỬA ĐỔI
    private final EventBroadcastRepository eventBroadcastRepository;
    private final BroadcastDeliveryRepository broadcastDeliveryRepository;

    // <<< SỬA ĐỔI HÀM TẠO (Constructor)
    public BroadcastServiceImpl(PartnerRepository partnerRepository, 
                                EventRepository eventRepository, 
                                CheckinRepository checkinRepository, // <<< SỬA ĐỔI
                                EventBroadcastRepository eventBroadcastRepository, 
                                BroadcastDeliveryRepository broadcastDeliveryRepository) {
        this.partnerRepository = partnerRepository;
        this.eventRepository = eventRepository;
        this.checkinRepository = checkinRepository; // <<< SỬA ĐỔI
        this.eventBroadcastRepository = eventBroadcastRepository;
        this.broadcastDeliveryRepository = broadcastDeliveryRepository;
    }

    @Override
    @Transactional
    public EventBroadcastResponseDTO sendBroadcast(Long partnerId, BroadcastRequestDTO requestDTO) {
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
        // <<< SỬA ĐỔI LOGIC LẤY DANH SÁCH
        List<Checkin> checkins = checkinRepository.findAllByEventId(event.getId());

        // 5. Tạo các bản ghi delivery cho từng sinh viên
        if (!checkins.isEmpty()) {
            List<BroadcastDelivery> deliveries = checkins.stream() // <<< SỬA ĐỔI: Dùng checkins
                    .map(checkin -> { // <<< SỬA ĐỔI: Dùng checkin
                        BroadcastDelivery delivery = new BroadcastDelivery();
                        delivery.setBroadcast(savedBroadcast);
                        delivery.setStudent(checkin.getStudent()); // <<< SỬA ĐỔI: Lấy student từ checkin
                        delivery.setStatus("SENT"); // Trạng thái ban đầu
                        return delivery;
                    })
                    .collect(Collectors.toList());
            
            broadcastDeliveryRepository.saveAll(deliveries);
        }
        
        // (Trong tương lai, đây là lúc trigger gửi Push Notification/Email)

        return convertToDTO(savedBroadcast);
    }
    
    // Helper method (Giữ nguyên)
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