package com.example.demo.service.impl;

import com.example.demo.config.AuthPrincipal; 
import com.example.demo.dto.request.CheckinRequestDTO;
import com.example.demo.dto.response.CheckinResponseDTO;
import com.example.demo.dto.response.StudentResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.CheckinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional; 

@Service
public class CheckinServiceImpl implements CheckinService {

    private static final Logger logger = LoggerFactory.getLogger(CheckinServiceImpl.class);

    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final CheckinRepository checkinRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final StudentServiceImpl studentService; 
    private final PartnerRepository partnerRepository; 

    public CheckinServiceImpl(EventRepository eventRepository, StudentRepository studentRepository, 
                              CheckinRepository checkinRepository, WalletRepository walletRepository, 
                              WalletTransactionRepository transactionRepository, 
                              StudentServiceImpl studentService, 
                              PartnerRepository partnerRepository) { 
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
        this.checkinRepository = checkinRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.studentService = studentService; 
        this.partnerRepository = partnerRepository; 
    }

    // (Phương thức registerEvent giữ nguyên, logic đã đúng)
    @Override
    @Transactional
    public CheckinResponseDTO registerEvent(String cognitoSub, Long eventId) {
        // ... (Toàn bộ logic registerEvent của bạn)
        Student student = studentRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        
        Wallet studentWallet = walletRepository.findByOwnerTypeAndOwnerId("STUDENT", student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student wallet not found."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
        
        Wallet eventWallet = event.getWallet();
        if (eventWallet == null) {
            throw new ResourceNotFoundException("Event wallet not found for this event.");
        }
        
        if (!"ACTIVE".equals(event.getStatus())) {
             throw new ForbiddenException("Event is not active.");
        }
        if (event.getStartTime().isBefore(OffsetDateTime.now())) {
            throw new ForbiddenException("Event has already started.");
        }
        if (checkinRepository.existsByEventIdAndStudentId(eventId, student.getId())) {
            throw new DataIntegrityViolationException("Student has already registered.");
        }
        Integer registeredCount = checkinRepository.countByEventId(eventId);
        if (event.getMaxAttendees() != null && event.getMaxAttendees() > 0 && registeredCount >= event.getMaxAttendees()) {
            throw new ForbiddenException("Event is fully booked.");
        }

        Integer depositPoints = event.getPointCostToRegister() != null ? event.getPointCostToRegister() : 0;
        BigDecimal depositAmount = new BigDecimal(depositPoints);
        
        if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (studentWallet.getBalance().compareTo(depositAmount) < 0) {
                throw new ForbiddenException("Insufficient points for deposit (" + depositPoints + " points).");
            }
            
            studentWallet.setBalance(studentWallet.getBalance().subtract(depositAmount));
            eventWallet.setBalance(eventWallet.getBalance().add(depositAmount));
            
            walletRepository.save(studentWallet);
            walletRepository.save(eventWallet);
            
            WalletTransaction studentTx = new WalletTransaction();
            studentTx.setWallet(studentWallet);
            studentTx.setCounterparty(eventWallet); 
            studentTx.setTxnType("EVENT_DEPOSIT");
            studentTx.setAmount(depositAmount.negate()); 
            studentTx.setReferenceType("EVENT");
            studentTx.setReferenceId(event.getId());
            transactionRepository.save(studentTx);

            WalletTransaction eventTx = new WalletTransaction();
            eventTx.setWallet(eventWallet);
            eventTx.setCounterparty(studentWallet); 
            eventTx.setTxnType("RECEIVE_DEPOSIT");
            eventTx.setAmount(depositAmount); 
            eventTx.setReferenceType("EVENT");
            eventTx.setReferenceId(event.getId());
            transactionRepository.save(eventTx);
            
            logger.info("Student {} paid {} points deposit to Event {}", student.getId(), depositAmount, eventId);
        }

        Checkin registration = new Checkin();
        registration.setEvent(event);
        registration.setStudent(student);
        if (student.getPhoneNumber() == null) {
            throw new DataIntegrityViolationException("Student must have a phone number to register.");
        }
        registration.setPhoneNumber(student.getPhoneNumber());
        registration.setVerified(false); 
        
        registration.setCheckinTime(OffsetDateTime.now());
        
        Checkin savedRegistration = checkinRepository.save(registration);
        logger.info("Student {} successfully registered for event {}", student.getId(), eventId);

        return convertToResponseDTO(savedRegistration, depositPoints);
    }
    
    // =================================================================
    // API ĐIỂM DANH (performCheckin) - (Đã sửa lỗi log)
    // =================================================================
    @Override
    @Transactional
    public CheckinResponseDTO performCheckin(Long eventId, CheckinRequestDTO requestDTO, AuthPrincipal principal) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        
        checkEventOwnership(event, principal);

        Student student = studentRepository.findByPhoneNumber(requestDTO.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with phone: " + requestDTO.getPhoneNumber()));

        Checkin checkinRecord = checkinRepository.findByEventIdAndStudentId(eventId, student.getId())
                .orElseThrow(() -> new DataIntegrityViolationException("Check-in failed: This student is not registered."));

        if (Boolean.TRUE.equals(checkinRecord.getVerified())) {
            throw new DataIntegrityViolationException("Student has already checked in.");
        }

        checkinRecord.setVerified(true);
        Checkin savedCheckin = checkinRepository.save(checkinRecord);
        
        // ======================================================
        // SỬA LỖI TẠI ĐÂY
        // ======================================================
        
        // Thay thế principal.getName() bằng principal.getCognitoSub() (hoặc partnerId)
        logger.info("Student {} (via Partner/Admin CognitoSub: {}) successfully CHECKED IN for event {}", 
            student.getId(), principal.getCognitoSub(), eventId);
        
        // ======================================================

        Integer depositPoints = event.getPointCostToRegister();
        return convertToResponseDTO(savedCheckin, depositPoints);
    }
    
    // (Phương thức getAttendeesByEvent giữ nguyên)
    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponseDTO> getAttendeesByEvent(Long eventId, Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        
        Page<Checkin> checkins = checkinRepository.findAllByEventId(eventId, pageable);
        
        return checkins.map(checkin -> studentService.toResponseDTO(checkin.getStudent()));
    }

    // (Helper checkEventOwnership giữ nguyên)
    private void checkEventOwnership(Event event, AuthPrincipal principal) {
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTNERS"))) {
            Long partnerIdFromToken = principal.getPartnerId();
            
            if (partnerIdFromToken == null) { 
                Optional<Partner> partnerOpt = partnerRepository.findByCognitoSub(principal.getCognitoSub());
                if (partnerOpt.isPresent()) {
                    partnerIdFromToken = partnerOpt.get().getId();
                } else {
                    throw new ForbiddenException("Partner profile not found in token.");
                }
            }
            
            if (event.getPartner().getId().equals(partnerIdFromToken)) {
                return; 
            }
        }
        throw new ForbiddenException("You do not have permission to perform this action on this event.");
    }

    // (Helper convertToResponseDTO giữ nguyên)
    private CheckinResponseDTO convertToResponseDTO(Checkin checkin, Integer depositPaid) {
        CheckinResponseDTO dto = new CheckinResponseDTO();
        dto.setCheckinId(checkin.getId());
        dto.setEventId(checkin.getEvent().getId());
        dto.setEventTitle(checkin.getEvent().getTitle());
        dto.setStudentId(checkin.getStudent().getId());
        dto.setStudentName(checkin.getStudent().getFullName());
        dto.setRegistrationTime(checkin.getCheckinTime()); 
        dto.setVerified(checkin.getVerified());
        dto.setDepositPaid(depositPaid);
        return dto;
    }
}