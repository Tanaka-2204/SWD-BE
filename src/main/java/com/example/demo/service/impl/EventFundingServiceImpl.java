package com.example.demo.service.impl;

import com.example.demo.dto.request.EventFundingRequestDTO;
import com.example.demo.dto.response.EventFundingResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.EventFundingService;
import com.example.demo.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EventFundingServiceImpl implements EventFundingService {

    private final PartnerRepository partnerRepository;
    private final EventRepository eventRepository;
    private final WalletRepository walletRepository;
    private final EventFundingRepository eventFundingRepository;
    private final WalletTransactionRepository transactionRepository;

    public EventFundingServiceImpl(PartnerRepository partnerRepository, EventRepository eventRepository,
                                   WalletRepository walletRepository, EventFundingRepository eventFundingRepository,
                                   WalletTransactionRepository transactionRepository) {
        this.partnerRepository = partnerRepository;
        this.eventRepository = eventRepository;
        this.walletRepository = walletRepository;
        this.eventFundingRepository = eventFundingRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public EventFundingResponseDTO fundEvent(Long partnerId, EventFundingRequestDTO requestDTO) {
        BigDecimal amount = requestDTO.getAmount();

        // 1. Lấy Partner và Ví của họ
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + partnerId));

        Wallet partnerWallet = partner.getWallet();
        if (partnerWallet == null) {
            throw new ResourceNotFoundException("Wallet not found for partner: " + partnerId);
        }

        // 2. Lấy Sự kiện VÀ VÍ SỰ KIỆN
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + requestDTO.getEventId()));

        // <<< SỬA ĐỔI: Lấy ví của Event
        Wallet eventWallet = event.getWallet();
        if (eventWallet == null) {
            // (Bạn nên có logic tạo ví cho sự kiện khi sự kiện được tạo)
            throw new ResourceNotFoundException("Event Wallet not found for this event. Event might not be initialized properly.");
        }

        // 3. Kiểm tra quyền (Giữ nguyên)
        if (!event.getPartner().getId().equals(partnerId)) {
            throw new ForbiddenException("Partner does not own this event.");
        }

        // 4. Kiểm tra số dư Partner (Giữ nguyên)
        if (partnerWallet.getBalance().compareTo(amount) < 0) {
            throw new DataIntegrityViolationException(
                    "Insufficient funds. Partner balance is: " + partnerWallet.getBalance());
        }

        // 5. <<< SỬA ĐỔI: Thực hiện giao dịch (Partner -> Event)
        
        // 5.1. Trừ tiền ví Partner
        partnerWallet.setBalance(partnerWallet.getBalance().subtract(amount));
        
        // 5.2. Cộng tiền vào VÍ EVENT
        eventWallet.setBalance(eventWallet.getBalance().add(amount));

        // 5.3. Cập nhật tổng ngân sách (chỉ để theo dõi)
        BigDecimal newTotalBudget = event.getTotalBudgetCoin().add(amount);
        event.setTotalBudgetCoin(newTotalBudget);

        // 5.4. TÍNH TOÁN MAX ATTENDEES (Giữ nguyên logic cũ)
        Integer totalRewardPoints = event.getTotalRewardPoints(); 
        BigDecimal rewardPerAttendee = totalRewardPoints != null ? new BigDecimal(totalRewardPoints) : BigDecimal.ZERO;

        if (rewardPerAttendee.compareTo(BigDecimal.ZERO) > 0) {
            Integer maxSlots = newTotalBudget.divide(rewardPerAttendee, 0, RoundingMode.FLOOR).intValue();
            event.setMaxAttendees(maxSlots);
        } else {
            event.setMaxAttendees(0); 
        }
        
        // 5.5. Lưu các thay đổi
        walletRepository.save(partnerWallet);
        walletRepository.save(eventWallet); // <<< SỬA ĐỔI: Lưu ví event
        eventRepository.save(event); 

        // 6. Ghi lại lịch sử cấp vốn (Giữ nguyên)
        EventFunding funding = new EventFunding();
        funding.setPartner(partner);
        funding.setEvent(event);
        funding.setAmountCoin(amount);
        EventFunding savedFunding = eventFundingRepository.save(funding);

        // 7. <<< SỬA ĐỔI: Ghi lại 2 giao dịch ví (Partner và Event)
        
        // 7.1. Giao dịch cho Partner (Trừ tiền)
        WalletTransaction partnerTx = new WalletTransaction();
        partnerTx.setWallet(partnerWallet);
        partnerTx.setCounterparty(eventWallet); // <<< Đối tác là Ví Event
        partnerTx.setTxnType("FUND_EVENT"); // (Partner chi tiền)
        partnerTx.setAmount(amount.negate()); // Ghi âm
        partnerTx.setReferenceType("EVENT_FUNDING");
        partnerTx.setReferenceId(savedFunding.getId());
        transactionRepository.save(partnerTx);
        
        // 7.2. Giao dịch cho Event (Nhận tiền)
        WalletTransaction eventTx = new WalletTransaction();
        eventTx.setWallet(eventWallet);
        eventTx.setCounterparty(partnerWallet); // <<< Đối tác là Ví Partner
        eventTx.setTxnType("RECEIVE_FUNDING"); // (Event nhận tiền)
        eventTx.setAmount(amount); // Ghi dương
        eventTx.setReferenceType("EVENT_FUNDING");
        eventTx.setReferenceId(savedFunding.getId());
        transactionRepository.save(eventTx);

        // 8. Trả về DTO (Giữ nguyên)
        return convertToDTO(savedFunding);
    }

    // Helper method
    private EventFundingResponseDTO convertToDTO(EventFunding funding) {
        EventFundingResponseDTO dto = new EventFundingResponseDTO();
        dto.setId(funding.getId());
        dto.setAmountCoin(funding.getAmountCoin());
        dto.setCreatedAt(funding.getCreatedAt());

        if (funding.getEvent() != null) {
            dto.setEventId(funding.getEvent().getId());
        }

        if (funding.getPartner() != null) {
            dto.setPartnerId(funding.getPartner().getId());
            dto.setPartnerName(funding.getPartner().getName());
        }

        return dto;
    }
}