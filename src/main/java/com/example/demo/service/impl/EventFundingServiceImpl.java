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

        // 2. Lấy Sự kiện
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + requestDTO.getEventId()));

        // 3. Kiểm tra quyền: Partner này có phải người tổ chức sự kiện không?
        if (!event.getPartner().getId().equals(partnerId)) {
            throw new ForbiddenException("Partner does not own this event.");
        }

        // 4. Kiểm tra số dư
        if (partnerWallet.getBalance().compareTo(amount) < 0) {
            throw new DataIntegrityViolationException(
                    "Insufficient funds. Partner balance is: " + partnerWallet.getBalance());
        }

        // 5. Thực hiện giao dịch và Cập nhật ngân sách
        // Trừ tiền ví Partner
        partnerWallet.setBalance(partnerWallet.getBalance().subtract(amount));
        walletRepository.save(partnerWallet);

        // Cộng ngân sách vào Event
        BigDecimal newTotalBudget = event.getTotalBudgetCoin().add(amount);
        event.setTotalBudgetCoin(newTotalBudget);

        // 5.1. TÍNH TOÁN MAX ATTENDEES (SỬ DỤNG totalRewardPoints)
        Integer totalRewardPoints = event.getTotalRewardPoints(); 
        BigDecimal rewardPerAttendee = totalRewardPoints != null ? new BigDecimal(totalRewardPoints) : BigDecimal.ZERO;

        // Chỉ tính MaxAttendees nếu điểm thưởng > 0 (nghĩa là ngân sách này dùng để thưởng)
        if (rewardPerAttendee.compareTo(BigDecimal.ZERO) > 0) {
            // Chia Tổng Ngân sách (Coin) cho Điểm thưởng trên mỗi người, làm tròn xuống
            Integer maxSlots = newTotalBudget.divide(rewardPerAttendee, 0, RoundingMode.FLOOR).intValue();
            event.setMaxAttendees(maxSlots);
        } else {
            // Nếu không có thưởng, không thể xác định max attendees từ ngân sách, đặt là 0
            event.setMaxAttendees(0); 
        }
        
        eventRepository.save(event); // Lưu sự kiện

        // 6. Ghi lại lịch sử cấp vốn
        EventFunding funding = new EventFunding();
        funding.setPartner(partner);
        funding.setEvent(event);
        funding.setAmountCoin(amount);
        EventFunding savedFunding = eventFundingRepository.save(funding);

        // 7. Ghi lại lịch sử giao dịch ví (cho Partner)
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(partnerWallet);
        transaction.setTxnType("PARTNER_FUND_EVENT");
        transaction.setAmount(amount.negate()); // Ghi âm (trừ tiền)
        transaction.setReferenceType("EVENT_FUNDING");
        transaction.setReferenceId(savedFunding.getId());
        transactionRepository.save(transaction);

        // 8. Trả về DTO
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