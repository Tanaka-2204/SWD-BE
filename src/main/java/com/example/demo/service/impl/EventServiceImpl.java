package com.example.demo.service.impl;

import com.example.demo.config.AuthPrincipal;
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
import com.example.demo.exception.DataIntegrityViolationException; 
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList; 
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
        
        // 1. Kiểm tra quyền và tìm Partner (Helper đã bao gồm logic xác thực)
        Partner partnerToAssign = findPartnerFromJwt(jwt, requestDTO.getPartnerId());
        
        Wallet partnerWallet = partnerToAssign.getWallet();
        if (partnerWallet == null) {
            throw new ResourceNotFoundException("Wallet not found for partner: " + partnerToAssign.getId());
        }

        BigDecimal fundingAmount = requestDTO.getTotalBudgetCoin();
        if (fundingAmount == null || fundingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DataIntegrityViolationException("Initial budget (totalBudgetCoin) must be zero or positive.");
        }

        if (fundingAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (partnerWallet.getBalance().compareTo(fundingAmount) < 0) {
                throw new DataIntegrityViolationException(
                        "Insufficient funds. Partner balance is: " + partnerWallet.getBalance());
            }
        }

        // 2. Tìm danh mục
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
        event.setStartTime(requestDTO.getStartTime());
        event.setEndTime(requestDTO.getEndTime());
        event.setLocation(requestDTO.getLocation());
        event.setPointCostToRegister(requestDTO.getPointCostToRegister());
        event.setTotalRewardPoints(requestDTO.getTotalRewardPoints());
        
        event.setTotalBudgetCoin(fundingAmount); 
        event.setStatus("DRAFT"); 

        // 3.1. Tạo Ví Event
        Wallet eventWallet = new Wallet();
        eventWallet.setBalance(fundingAmount); 
        eventWallet.setOwnerType("EVENT"); 
        event.setWallet(eventWallet); 
        
        // 3.2. Tính Max Attendees
        Integer totalRewardPoints = event.getTotalRewardPoints(); 
        BigDecimal rewardPerAttendee = totalRewardPoints != null ? new BigDecimal(totalRewardPoints) : BigDecimal.ZERO;

        if (rewardPerAttendee.compareTo(BigDecimal.ZERO) > 0 && fundingAmount.compareTo(BigDecimal.ZERO) > 0) {
            Integer maxSlots = fundingAmount.divide(rewardPerAttendee, 0, RoundingMode.FLOOR).intValue();
            event.setMaxAttendees(maxSlots);
        } else {
            event.setMaxAttendees(0); 
        }

        // 4. Thực hiện chuyển tiền (Nếu nạp > 0)
        if (fundingAmount.compareTo(BigDecimal.ZERO) > 0) {
            partnerWallet.setBalance(partnerWallet.getBalance().subtract(fundingAmount));
            walletRepository.save(partnerWallet);
        }
        
        // 5. Lưu sự kiện
        Event savedEvent = eventRepository.save(event);
        
        eventWallet.setOwnerId(savedEvent.getId());
        walletRepository.save(eventWallet); 

        // 6. Ghi giao dịch (Nếu nạp > 0)
        if (fundingAmount.compareTo(BigDecimal.ZERO) > 0) {
            WalletTransaction partnerTx = new WalletTransaction();
            partnerTx.setWallet(partnerWallet);
            partnerTx.setCounterparty(eventWallet); 
            partnerTx.setTxnType("FUND_EVENT"); 
            partnerTx.setAmount(fundingAmount.negate()); 
            partnerTx.setReferenceType("EVENT_CREATION");
            partnerTx.setReferenceId(savedEvent.getId());
            transactionRepository.save(partnerTx);
            
            WalletTransaction eventTx = new WalletTransaction();
            eventTx.setWallet(eventWallet);
            eventTx.setCounterparty(partnerWallet); 
            eventTx.setTxnType("RECEIVE_FUNDING"); 
            eventTx.setAmount(fundingAmount); 
            eventTx.setReferenceType("EVENT_CREATION");
            eventTx.setReferenceId(savedEvent.getId());
            transactionRepository.save(eventTx);
        }

        logger.info("Successfully created event '{}' (ID: {}) and its wallet (ID: {}) with initial budget {}", 
                savedEvent.getTitle(), savedEvent.getId(), eventWallet.getId(), fundingAmount);

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
    public EventResponseDTO updateEvent(Long eventId, EventUpdateDTO requestDTO, AuthPrincipal principal) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // <<< LOGIC BẢO MẬT: Kiểm tra quyền sở hữu
        checkEventOwnership(event, principal);

        boolean rewardChanged = false;

        // Cập nhật các trường
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
        if (requestDTO.getPointCostToRegister() != null) {
            event.setPointCostToRegister(requestDTO.getPointCostToRegister());
        }
        if (requestDTO.getTotalRewardPoints() != null) {
            event.setTotalRewardPoints(requestDTO.getTotalRewardPoints());
            rewardChanged = true; 
        }
        if (requestDTO.getCategoryId() != null) {
            EventCategory category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "EventCategory not found with id: " + requestDTO.getCategoryId()));
            event.setCategory(category);
        }

        // Tính toán lại MaxAttendees
        if (rewardChanged || requestDTO.getTotalBudgetCoin() != null) { 
            if (requestDTO.getTotalBudgetCoin() != null) {
                event.setTotalBudgetCoin(requestDTO.getTotalBudgetCoin());
            }
            BigDecimal totalBudget = event.getTotalBudgetCoin(); 
            Integer totalRewardPoints = event.getTotalRewardPoints();
            BigDecimal rewardPerAttendee = totalRewardPoints != null ? new BigDecimal(totalRewardPoints)
                    : BigDecimal.ZERO;
            if (rewardPerAttendee.compareTo(BigDecimal.ZERO) > 0 && totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                Integer maxSlots = totalBudget.divide(rewardPerAttendee, 0, RoundingMode.FLOOR).intValue();
                event.setMaxAttendees(maxSlots);
            } else {
                event.setMaxAttendees(0);
            }
        }
        
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    // --- DELETE ---
    @Override
    @Transactional
    public void deleteEvent(Long eventId, AuthPrincipal principal) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        
        // <<< LOGIC BẢO MẬT: Kiểm tra quyền sở hữu
        checkEventOwnership(event, principal);
        
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

    // ==========================================================
    // PHƯƠNG THỨC FINALIZE EVENT (Logic Ví Event)
    // ==========================================================
    @Override
    @Transactional
    public EventResponseDTO finalizeEvent(Long eventId, AuthPrincipal principal) {
        // 1. Tìm sự kiện
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // <<< LOGIC BẢO MẬT: Kiểm tra quyền sở hữu
        checkEventOwnership(event, principal);
        
        // 2. Kiểm tra trạng thái
        if ("FINALIZED".equals(event.getStatus())) {
            throw new DataIntegrityViolationException("Event has already been finalized.");
        }
        
        // 3. Xác định tổng số điểm cần chi trả (Hoàn cọc + Thưởng)
        Integer depositPoints = event.getPointCostToRegister() != null ? event.getPointCostToRegister() : 0;
        Integer rewardPoints = event.getTotalRewardPoints() != null ? event.getTotalRewardPoints() : 0;
        BigDecimal totalPayoutAmount = new BigDecimal(depositPoints + rewardPoints);

        if (totalPayoutAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Event {} has no payout amount. Finalizing without transactions.", eventId);
            event.setStatus("FINALIZED");
            Event savedEvent = eventRepository.save(event);
            return convertToDTO(savedEvent);
        }

        // 4. Lấy VÍ SỰ KIỆN (Event Wallet) làm nguồn tiền
        Wallet eventWallet = event.getWallet();
        if (eventWallet == null) {
            throw new ResourceNotFoundException("CRITICAL: Event Wallet not found for this event.");
        }

        // 5. Lấy danh sách sinh viên đã check-in thành công (verified = true)
        List<Checkin> successfulCheckins = checkinRepository.findAllByEventIdAndVerifiedTrue(eventId);

        if (successfulCheckins.isEmpty()) {
            logger.warn("Event {} has no successful check-ins. Finalizing without transactions.", eventId);
            event.setStatus("FINALIZED");
            Event savedEvent = eventRepository.save(event);
            return convertToDTO(savedEvent);
        }

        // 6. Kiểm tra ngân sách VÍ SỰ KIỆN
        BigDecimal requiredBudget = totalPayoutAmount.multiply(new BigDecimal(successfulCheckins.size()));
        if (eventWallet.getBalance().compareTo(requiredBudget) < 0) {
            throw new ForbiddenException(
                    "Event wallet has insufficient funds to finalize rewards. Required: " + requiredBudget 
                    + ", Available: " + eventWallet.getBalance());
        }

        // 7. Thực hiện giao dịch (Event Wallet -> Student Wallet)
        List<WalletTransaction> transactionsToSave = new ArrayList<>();
        List<Wallet> studentWalletsToSave = new ArrayList<>(); 

        for (Checkin checkin : successfulCheckins) {
            Student student = checkin.getStudent();
            Optional<Wallet> studentWalletOpt = walletRepository.findByOwnerTypeAndOwnerId("STUDENT", student.getId());
            
            if (studentWalletOpt.isEmpty()) {
                logger.error("Student wallet not found for student ID: {}. Skipping payout.", student.getId());
                continue; 
            }
            Wallet studentWallet = studentWalletOpt.get();

            eventWallet.setBalance(eventWallet.getBalance().subtract(totalPayoutAmount));
            studentWallet.setBalance(studentWallet.getBalance().add(totalPayoutAmount));
            studentWalletsToSave.add(studentWallet);

            WalletTransaction studentTx = new WalletTransaction();
            studentTx.setWallet(studentWallet);
            studentTx.setCounterparty(eventWallet); 
            studentTx.setTxnType("EVENT_FINAL_PAYOUT"); 
            studentTx.setAmount(totalPayoutAmount);
            studentTx.setReferenceType("EVENT");
            studentTx.setReferenceId(event.getId());
            transactionsToSave.add(studentTx);

            WalletTransaction eventTx = new WalletTransaction();
            eventTx.setWallet(eventWallet);
            eventTx.setCounterparty(studentWallet); 
            eventTx.setTxnType("EVENT_PAYOUT");
            eventTx.setAmount(totalPayoutAmount.negate()); 
            eventTx.setReferenceType("EVENT");
            eventTx.setReferenceId(event.getId());
            transactionsToSave.add(eventTx);
        }

        // 8. Lưu tất cả thay đổi (Tối ưu hóa)
        walletRepository.save(eventWallet); 
        walletRepository.saveAll(studentWalletsToSave); 
        transactionRepository.saveAll(transactionsToSave); 

        // 9. Cập nhật trạng thái sự kiện
        event.setStatus("FINALIZED");
        Event savedEvent = eventRepository.save(event);

        return convertToDTO(savedEvent);
    }

    // ==========================================================
    // HELPER METHODS
    // ==========================================================

    /**
     * Helper bảo mật: Kiểm tra quyền sở hữu sự kiện
     */
    private void checkEventOwnership(Event event, AuthPrincipal principal) {
        // 1. Nếu là Admin, cho phép
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        // 2. Nếu là Partner, kiểm tra ID
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTNER"))) {
            Long partnerIdFromToken = principal.getPartnerId();
            if (partnerIdFromToken == null) {
                throw new ForbiddenException("Partner ID not found in token.");
            }
            if (event.getPartner().getId().equals(partnerIdFromToken)) {
                return; // Đúng chủ sở hữu
            }
        }
        
        // 3. Nếu không phải cả hai, từ chối
        throw new ForbiddenException("You do not have permission to perform this action on this event.");
    }

    /**
     * Helper (Dùng trong createEvent): Tìm Partner từ Jwt và kiểm tra quyền
     */
    private Partner findPartnerFromJwt(Jwt jwt, Long requestedPartnerId) {
        String cognitoSub = jwt.getSubject();
        Collection<? extends GrantedAuthority> authorities = jwtAuthenticationConverter.convert(jwt).getAuthorities();
        boolean isAdmin = authorities.stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        boolean isPartner = authorities.stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_PARTNERS"));
        
        if (isAdmin) {
            return partnerRepository.findById(requestedPartnerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Partner specified in request (ID: " + requestedPartnerId + ") not found."));
        } else if (isPartner) {
            Partner loggedInPartner = partnerRepository.findByCognitoSub(cognitoSub)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Partner profile not found for authenticated user."));
            if (!loggedInPartner.getId().equals(requestedPartnerId)) {
                throw new ForbiddenException("Partners can only create events for themselves. Mismatched partnerId.");
            }
            return loggedInPartner;
        } else {
            throw new ForbiddenException(
                    "User does not have permission (Admin or Partner role required) to create events.");
        }
    }

    /**
     * Helper: Chuyển đổi Entity sang DTO
     */
    private EventResponseDTO convertToDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus());
        dto.setPointCostToRegister(event.getPointCostToRegister());
        dto.setTotalRewardPoints(event.getTotalRewardPoints());
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