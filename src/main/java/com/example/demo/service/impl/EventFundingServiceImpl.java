package com.example.demo.service.impl;

// ... (Tất cả các import cần thiết)
import com.example.demo.service.EventFundingService;
import com.example.demo.dto.request.EventFundingRequestDTO;
import com.example.demo.dto.response.EventFundingResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

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

        // 2. Lấy Sự kiện
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + requestDTO.getEventId()));

        // 3. Kiểm tra quyền: Partner này có phải người tổ chức sự kiện không?
        if (!event.getPartner().getId().equals(partnerId)) {
            // Nếu bạn cho phép partner khác tài trợ, hãy bỏ qua bước này
            throw new ForbiddenException("Partner does not own this event.");
        }

        // 4. Kiểm tra số dư
        if (partnerWallet.getBalance().compareTo(amount) < 0) {
            throw new DataIntegrityViolationException("Insufficient funds.");
        }

        // 5. Thực hiện giao dịch
        // Trừ tiền ví Partner
        partnerWallet.setBalance(partnerWallet.getBalance().subtract(amount));
        walletRepository.save(partnerWallet);

        // Cộng ngân sách vào Event
        event.setTotalBudgetCoin(event.getTotalBudgetCoin().add(amount));
        eventRepository.save(event);

        // 6. Ghi lại lịch sử cấp vốn
        EventFunding funding = new EventFunding();
        funding.setPartner(partner);
        funding.setEvent(event);
        funding.setAmountCoin(amount);
        EventFunding savedFunding = eventFundingRepository.save(funding);

        // 7. Ghi lại lịch sử giao dịch ví
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(partnerWallet);
        transaction.setTxnType("PARTNER_FUND_EVENT");
        transaction.setAmount(amount.negate()); // Ghi âm (trừ tiền)
        transaction.setReferenceType("EVENT_FUNDING");
        transaction.setReferenceId(savedFunding.getId());
        transactionRepository.save(transaction);

        return convertToDTO(savedFunding); // Bạn cần tự tạo hàm convertToDTO
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