package com.example.demo.service.impl;

import com.example.demo.dto.request.EventCreateDTO;
import com.example.demo.dto.request.EventUpdateDTO;
import com.example.demo.dto.response.EventCategoryResponseDTO;
import com.example.demo.dto.response.EventResponseDTO;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventCategory;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Student;
import com.example.demo.entity.Wallet;
import com.example.demo.entity.WalletTransaction;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EventCategoryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.WalletTransactionRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.ForbiddenException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final EventCategoryRepository categoryRepository;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final WalletTransactionRepository transactionRepository;
    private final CheckinRepository checkinRepository;
    private final WalletRepository walletRepository;

    public EventServiceImpl(EventRepository eventRepository,
            PartnerRepository partnerRepository,
            EventCategoryRepository categoryRepository, JwtAuthenticationConverter jwtAuthenticationConverter,
            WalletTransactionRepository transactionRepository, CheckinRepository checkinRepository,
            WalletRepository walletRepository) {
        this.eventRepository = eventRepository;
        this.partnerRepository = partnerRepository;
        this.categoryRepository = categoryRepository;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.transactionRepository = transactionRepository;
        this.checkinRepository = checkinRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public EventResponseDTO createEvent(Jwt jwt, EventCreateDTO requestDTO) {
        String cognitoSub = jwt.getSubject();
        Collection<? extends GrantedAuthority> authorities = jwtAuthenticationConverter.convert(jwt).getAuthorities();
        boolean isAdmin = authorities.stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        boolean isPartner = authorities.stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_PARTNERS"));

        Long requestedPartnerId = requestDTO.getPartnerId();

        logger.info("User {} (Admin: {}, Partner: {}) attempting to create event for partnerId {}",
                cognitoSub, isAdmin, isPartner, requestedPartnerId);

        Partner partnerToAssign;

        // 1. Kiểm tra quyền hạn và xác định Partner
        if (isAdmin) {
            logger.info("Admin {} is creating event for partnerId {}.", cognitoSub, requestedPartnerId);
            partnerToAssign = partnerRepository.findById(requestedPartnerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Partner specified in request (ID: " + requestedPartnerId + ") not found."));
        } else if (isPartner) {
            Partner loggedInPartner = partnerRepository.findByCognitoSub(cognitoSub)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Partner profile not found for authenticated user. Ensure cognitoSub is linked in Partner table."));
            if (!loggedInPartner.getId().equals(requestedPartnerId)) {
                logger.warn("Forbidden: Partner {} (ID: {}) attempted to create event for different partnerId {}",
                        cognitoSub, loggedInPartner.getId(), requestedPartnerId);
                throw new ForbiddenException("Partners can only create events for themselves. Mismatched partnerId.");
            }
            logger.info("Partner {} (ID: {}) is creating event for themselves.", cognitoSub, loggedInPartner.getId());
            partnerToAssign = loggedInPartner;
        } else {
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
        event.setPartner(partnerToAssign);
        event.setCategory(category);
        event.setTitle(requestDTO.getTitle());
        event.setDescription(requestDTO.getDescription());
        
        if (requestDTO.getStartTime() == null || requestDTO.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }

        // Gán giá trị được người dùng cung cấp trong Request DTO
        event.setStartTime(requestDTO.getStartTime());
        event.setEndTime(requestDTO.getEndTime());
        event.setLocation(requestDTO.getLocation());

        // <<< SỬA ĐỔI: THAY rewardPerCheckin BẰNG pointCostToRegister VÀ
        // totalRewardPoints >>>
        event.setPointCostToRegister(requestDTO.getPointCostToRegister());
        event.setTotalRewardPoints(requestDTO.getTotalRewardPoints());
        // event.setRewardPerCheckin(requestDTO.getRewardPerCheckin()); // ĐÃ XÓA

        event.setTotalBudgetCoin(requestDTO.getTotalBudgetCoin());
        event.setStatus("DRAFT"); // Trạng thái ban đầu

        // 4. Lưu sự kiện
        Event savedEvent = eventRepository.save(event);
        logger.info("Successfully created event '{}' (ID: {}) for partner ID {}", savedEvent.getTitle(),
                savedEvent.getId(), partnerToAssign.getId());

        return convertToDTO(savedEvent);
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
        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
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

        // Cập nhật điểm cọc
        if (requestDTO.getPointCostToRegister() != null) {
            event.setPointCostToRegister(requestDTO.getPointCostToRegister());
        }

        // Cập nhật mức thưởng và đánh dấu (LOGIC CŨ ĐÃ BỊ LOẠI BỎ)
        // <<< SỬA ĐỔI: Cập nhật totalRewardPoints và đánh dấu thay đổi >>>
        if (requestDTO.getTotalRewardPoints() != null) {
            event.setTotalRewardPoints(requestDTO.getTotalRewardPoints());
            rewardChanged = true; // Đánh dấu là reward đã thay đổi
        }

        // Cập nhật category (nếu có)
        if (requestDTO.getCategoryId() != null) {
            EventCategory category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "EventCategory not found with id: " + requestDTO.getCategoryId()));
            event.setCategory(category);
        }

        // 3. LOGIC MỚI: TÍNH TOÁN LẠI MAX ATTENDEES NẾU ĐIỂM THƯỞNG/NGÂN SÁCH THAY ĐỔI
        // Lưu ý: Request DTO không bao gồm cập nhật ngân sách (totalBudgetCoin).
        // Logic dưới đây chỉ tính lại nếu totalRewardPoints thay đổi.
        if (rewardChanged || requestDTO.getTotalBudgetCoin() != null) { // Kiểm tra nếu ngân sách cũng thay đổi (từ
                                                                        // EventUpdateDTO)
            if (requestDTO.getTotalBudgetCoin() != null) {
                event.setTotalBudgetCoin(requestDTO.getTotalBudgetCoin());
            }

            BigDecimal totalBudget = event.getTotalBudgetCoin(); // Lấy ngân sách hiện tại
            Integer totalRewardPoints = event.getTotalRewardPoints();
            BigDecimal rewardPerAttendee = totalRewardPoints != null ? new BigDecimal(totalRewardPoints)
                    : BigDecimal.ZERO;

            // Chỉ tính toán nếu có thưởng
            if (rewardPerAttendee.compareTo(BigDecimal.ZERO) > 0 && totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                // Chia ngân sách cho mức thưởng, làm tròn xuống
                Integer maxSlots = totalBudget.divide(rewardPerAttendee, 0, RoundingMode.FLOOR).intValue();
                event.setMaxAttendees(maxSlots);
            } else {
                // Nếu không có thưởng hoặc ngân sách = 0, set 0
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
    public EventResponseDTO finalizeEvent(Long eventId) {
        // 1. Tìm sự kiện
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // 2. Kiểm tra trạng thái và quyền
        if ("FINALIZED".equals(event.getStatus())) {
            throw new DataIntegrityViolationException("Event has already been finalized.");
        }
        // (LƯU Ý: Cần thêm logic kiểm tra thời gian sự kiện đã kết thúc và kiểm tra
        // quyền ADMIN/PARTNER)

        // 3. Xác định tổng số điểm cần chi trả cho mỗi người tham dự
        Integer depositPoints = event.getPointCostToRegister() != null ? event.getPointCostToRegister() : 0;
        Integer rewardPoints = event.getTotalRewardPoints() != null ? event.getTotalRewardPoints() : 0;

        // Tổng số tiền thưởng/hoàn trả cho mỗi người tham dự thành công
        BigDecimal totalPayoutAmount = new BigDecimal(depositPoints + rewardPoints);

        if (totalPayoutAmount.compareTo(BigDecimal.ZERO) <= 0) {
            // Nếu không có điểm cọc hoặc điểm thưởng, chỉ cập nhật trạng thái
            event.setStatus("FINALIZED");
            Event savedEvent = eventRepository.save(event);
            return convertToDTO(savedEvent);
        }

        // 4. Tìm Ví Partner (Nguồn tiền)
        Wallet partnerWallet = event.getPartner().getWallet();
        if (partnerWallet == null) {
            throw new ResourceNotFoundException("Partner wallet not found for event creator.");
        }

        // 5. Lấy danh sách sinh viên đã check-in thành công
        // Giả sử cột 'verified' trong bảng Checkin là true
        List<Checkin> successfulCheckins = checkinRepository.findAllByEventIdAndVerifiedTrue(eventId);

        if (successfulCheckins.isEmpty()) {
            logger.warn("Event {} has no successful check-ins. Finalizing without transactions.", eventId);
            event.setStatus("FINALIZED");
            Event savedEvent = eventRepository.save(event);
            return convertToDTO(savedEvent);
        }

        // 6. Kiểm tra ngân sách có đủ không
        BigDecimal requiredBudget = totalPayoutAmount.multiply(new BigDecimal(successfulCheckins.size()));
        if (partnerWallet.getBalance().compareTo(requiredBudget) < 0) {
            // Cần xử lý lỗi này tùy theo yêu cầu: hoặc ném lỗi, hoặc chỉ thanh toán một
            // phần
            throw new ForbiddenException(
                    "Partner wallet has insufficient funds to finalize rewards. Required: " + requiredBudget);
        }

        // 7. Thực hiện giao dịch cho từng sinh viên
        for (Checkin checkin : successfulCheckins) {
            Student student = checkin.getStudent();

            Optional<Wallet> studentWalletOpt = walletRepository.findByOwnerTypeAndOwnerId("STUDENT", student.getId());
            if (studentWalletOpt.isEmpty()) {
                logger.error("Student wallet not found for student ID: {}. Skipping payout.", student.getId());
                continue; // Bỏ qua sinh viên này
            }
            Wallet studentWallet = studentWalletOpt.get();

            // 7.1. Cập nhật số dư ví
            partnerWallet.setBalance(partnerWallet.getBalance().subtract(totalPayoutAmount));
            studentWallet.setBalance(studentWallet.getBalance().add(totalPayoutAmount));

            // 7.2. Ghi giao dịch cho Student (Nhận tiền)
            WalletTransaction studentTx = new WalletTransaction();
            studentTx.setWallet(studentWallet);
            studentTx.setCounterparty(partnerWallet);
            studentTx.setTxnType("EVENT_FINAL_PAYOUT");
            studentTx.setAmount(totalPayoutAmount);
            studentTx.setReferenceType("EVENT");
            studentTx.setReferenceId(event.getId());

            // 7.3. Ghi giao dịch cho Partner (Chi tiền)
            WalletTransaction partnerTx = new WalletTransaction();
            partnerTx.setWallet(partnerWallet);
            partnerTx.setCounterparty(studentWallet);
            partnerTx.setTxnType("EVENT_PAYOUT");
            partnerTx.setAmount(totalPayoutAmount.negate()); // Ghi âm
            partnerTx.setReferenceType("EVENT");
            partnerTx.setReferenceId(event.getId());

            transactionRepository.save(studentTx);
            transactionRepository.save(partnerTx);
        }

        // 8. Lưu các ví đã cập nhật (Partner và Student cuối cùng)
        walletRepository.save(partnerWallet);
        // Lưu ý: Các ví student đã được cập nhật số dư, nếu muốn tối ưu, bạn có thể
        // thu thập tất cả student wallets và lưu tất cả một lần sau vòng lặp.
        // Trong @Transactional, việc này thường là an toàn.

        // 9. Cập nhật trạng thái sự kiện
        event.setStatus("FINALIZED");

        Event savedEvent = eventRepository.save(event);

        return convertToDTO(savedEvent);
    }

    @Override
    @Transactional
    public EventResponseDTO approveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (!"PENDING".equals(event.getStatus())) {
            throw new IllegalStateException("Event is not in PENDING status");
        }

        event.setStatus("APPROVED");
        Event savedEvent = eventRepository.save(event);

        return convertToDTO(savedEvent);
    }

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

        // <<< SỬA ĐỔI: THAY rewardPerCheckin BẰNG pointCostToRegister VÀ
        // totalRewardPoints >>>
        dto.setPointCostToRegister(event.getPointCostToRegister());
        dto.setTotalRewardPoints(event.getTotalRewardPoints());
        // dto.setRewardPerCheckin(event.getRewardPerCheckin()); // ĐÃ XÓA

        dto.setTotalBudgetCoin(event.getTotalBudgetCoin());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setMaxAttendees(event.getMaxAttendees());

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