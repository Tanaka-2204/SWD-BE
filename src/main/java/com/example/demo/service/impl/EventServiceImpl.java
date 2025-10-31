package com.example.demo.service.impl;

import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventCategory;
import com.example.demo.entity.Partner;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EventCategoryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.WalletTransactionRepository;
import com.example.demo.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.ForbiddenException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.Collection;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final EventCategoryRepository categoryRepository;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final WalletTransactionRepository transactionRepository;

    public EventServiceImpl(EventRepository eventRepository,
            PartnerRepository partnerRepository,
            EventCategoryRepository categoryRepository, JwtAuthenticationConverter jwtAuthenticationConverter,
            WalletTransactionRepository transactionRepository) {
        this.eventRepository = eventRepository;
        this.partnerRepository = partnerRepository;
        this.categoryRepository = categoryRepository;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public EventResponseDTO createEvent(Jwt jwt, EventCreateDTO requestDTO) { // Chữ ký đúng
        String cognitoSub = jwt.getSubject();
        // Lấy roles từ JWT sử dụng converter
        Collection<? extends GrantedAuthority> authorities = jwtAuthenticationConverter.convert(jwt).getAuthorities();
        boolean isAdmin = authorities.stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN")); // Kiểm tra role ADMIN
        boolean isPartner = authorities.stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_PARTNERS")); // Kiểm tra role PARTNER

        Long requestedPartnerId = requestDTO.getPartnerId(); // Lấy partnerId từ DTO

        logger.info("User {} (Admin: {}, Partner: {}) attempting to create event for partnerId {}",
                cognitoSub, isAdmin, isPartner, requestedPartnerId);

        Partner partnerToAssign; // Partner sẽ được gán cho sự kiện

        // 1. Kiểm tra quyền hạn và xác định Partner
        if (isAdmin) {
            // Admin có quyền tạo cho bất kỳ partner nào hợp lệ
            logger.info("Admin {} is creating event for partnerId {}.", cognitoSub, requestedPartnerId);
            partnerToAssign = partnerRepository.findById(requestedPartnerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Partner specified in request (ID: " + requestedPartnerId + ") not found."));
        } else if (isPartner) {
            // Partner chỉ được tạo cho chính mình
            Partner loggedInPartner = partnerRepository.findByCognitoSub(cognitoSub)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Partner profile not found for authenticated user. Ensure cognitoSub is linked in Partner table."));
            if (!loggedInPartner.getId().equals(requestedPartnerId)) {
                logger.warn("Forbidden: Partner {} (ID: {}) attempted to create event for different partnerId {}",
                        cognitoSub, loggedInPartner.getId(), requestedPartnerId);
                throw new ForbiddenException("Partners can only create events for themselves. Mismatched partnerId.");
            }
            logger.info("Partner {} (ID: {}) is creating event for themselves.", cognitoSub, loggedInPartner.getId());
            partnerToAssign = loggedInPartner; // Gán chính partner đang đăng nhập
        } else {
            // Vai trò không hợp lệ
            logger.error("Unauthorized role attempting to create event: User {}", cognitoSub);
            throw new ForbiddenException(
                    "User does not have permission (Admin or Partner role required) to create events.");
        }

        // 2. Tìm danh mục (nếu có)
        EventCategory category = null;
        if (requestDTO.getCategoryId() != null) {
            category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "EventCategory not found with id: " + requestDTO.getCategoryId()));
        }

        // 3. Tạo đối tượng Event
        Event event = new Event();
        event.setPartner(partnerToAssign); // Gán Partner đã xác định ở bước 1
        event.setCategory(category);
        event.setTitle(requestDTO.getTitle());
        event.setDescription(requestDTO.getDescription());
        event.setStartTime(requestDTO.getStartTime());
        event.setEndTime(requestDTO.getEndTime());
        event.setLocation(requestDTO.getLocation());
        event.setRewardPerCheckin(requestDTO.getRewardPerCheckin());
        event.setTotalBudgetCoin(requestDTO.getTotalBudgetCoin());
        event.setStatus("DRAFT"); // Trạng thái ban đầu

        // 4. Lưu sự kiện
        Event savedEvent = eventRepository.save(event);
        logger.info("Successfully created event '{}' (ID: {}) for partner ID {}", savedEvent.getTitle(),
                savedEvent.getId(), partnerToAssign.getId());

        return convertToDTO(savedEvent); // Hàm helper giữ nguyên
    }

    // --- READ ---
    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return convertToDTO(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getAllEvents(Specification<Event> spec, Pageable pageable) {
        Page<Event> eventPage = eventRepository.findAll(spec, pageable); // Dùng findAll với Specification
        return eventPage.map(this::convertToDTO);
    }

    // --- UPDATE ---
    @Override
    @Transactional
    public EventResponseDTO updateEvent(Long eventId, EventUpdateDTO requestDTO) {
        // 1. Tìm sự kiện
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        
        boolean rewardChanged = false; // Biến cờ để kiểm tra

        // 2. Cập nhật các trường nếu chúng được cung cấp trong DTO
        if (requestDTO.getTitle() != null) {
            event.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getDescription() != null) {
            event.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getStartTime() != null) {
            event.setStartTime(requestDTO.getStartTime());
        }
        if (requestDTO.getEndTime() != null) {
            event.setEndTime(requestDTO.getEndTime());
        }
        if (requestDTO.getLocation() != null) {
            event.setLocation(requestDTO.getLocation());
        }
        if (requestDTO.getStatus() != null) {
            event.setStatus(requestDTO.getStatus());
        }

        // Cập nhật mức thưởng và đánh dấu
        if (requestDTO.getRewardPerCheckin() != null) {
            event.setRewardPerCheckin(requestDTO.getRewardPerCheckin());
            rewardChanged = true; // Đánh dấu là reward đã thay đổi
        }

        // Cập nhật category (nếu có)
        if (requestDTO.getCategoryId() != null) {
            EventCategory category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("EventCategory not found with id: " + requestDTO.getCategoryId()));
            event.setCategory(category);
        }
        
        // 3. LOGIC MỚI: TÍNH TOÁN LẠI NẾU CẦN
        // Nếu reward thay đổi, tính lại max attendees
        if (rewardChanged) {
            BigDecimal totalBudget = event.getTotalBudgetCoin(); // Lấy ngân sách hiện tại
            BigDecimal reward = event.getRewardPerCheckin(); // Lấy mức thưởng mới

            // Chỉ tính toán nếu có thưởng
            if (reward != null && reward.compareTo(BigDecimal.ZERO) > 0) {
                // Chia ngân sách cho mức thưởng, làm tròn xuống
                Integer maxSlots = totalBudget.divide(reward, 0, RoundingMode.FLOOR).intValue();
                event.setMaxAttendees(maxSlots);
            } else {
                // Nếu không có thưởng (hoặc thưởng = 0), set 0
                event.setMaxAttendees(0);
            }
        }
        // <<< KẾT THÚC LOGIC MỚI >>>
        
        // 4. Lưu sự kiện
        Event savedEvent = eventRepository.save(event);
        
        // 5. Trả về DTO đã cập nhật
        return convertToDTO(savedEvent);
    }

    // --- DELETE ---
    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        // Lưu ý: Cần logic kiểm tra xem sự kiện có đang hoạt động hoặc có người đăng ký
        // không trước khi xóa.
        eventRepository.deleteById(eventId);
    }

    // --- BUSINESS LOGIC IMPLEMENTATIONS ---
    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getEventsByPartner(Long partnerId, Pageable pageable) {
        if (!partnerRepository.existsById(partnerId)) {
            throw new ResourceNotFoundException("Partner not found with id: " + partnerId);
        }
        return eventRepository.findAllByPartnerId(partnerId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("EventCategory not found with id: " + categoryId);
        }
        return eventRepository.findAllByCategoryId(categoryId).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findAllByStartTimeAfter(OffsetDateTime.now(), pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getOngoingEvents() {
        return eventRepository.findOngoingEvents(OffsetDateTime.now()).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> searchEventsByTitle(String keyword, Pageable pageable) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public EventResponseDTO updateEventStatus(Long eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Validate status - assuming valid statuses are APPROVED, REJECTED, etc.
        if (!List.of("APPROVED", "REJECTED", "DRAFT", "PUBLISHED", "CANCELLED").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        event.setStatus(status);
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    // --- HELPER METHOD ---
    // --- HELPER METHOD ---
    private EventResponseDTO convertToDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus());
        dto.setRewardPerCheckin(event.getRewardPerCheckin());
        dto.setTotalBudgetCoin(event.getTotalBudgetCoin());
        dto.setCreatedAt(event.getCreatedAt());

        // <<< DÒNG CÒN THIẾU MÀ BẠN CẦN THÊM LÀ ĐÂY >>>
        dto.setMaxAttendees(event.getMaxAttendees()); // Lấy dữ liệu từ và gán vào

        if (event.getPartner() != null) {
            dto.setPartnerId(event.getPartner().getId());
            dto.setPartnerName(event.getPartner().getName());
        }

        if (event.getCategory() != null) {
            EventCategoryResponseDTO categoryDTO = new EventCategoryResponseDTO();
            categoryDTO.setId(event.getCategory().getId());
            categoryDTO.setName(event.getCategory().getName());
            categoryDTO.setDescription(event.getCategory().getDescription());
            dto.setCategory(categoryDTO);
        }
        return dto;
    }
}