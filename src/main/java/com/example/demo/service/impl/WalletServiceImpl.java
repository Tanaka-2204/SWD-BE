package com.example.demo.service.impl;

import com.example.demo.dto.request.WalletTopupRequestDTO;
import com.example.demo.dto.response.WalletResponseDTO;
import com.example.demo.dto.response.WalletTransactionResponseDTO;
import com.example.demo.entity.Partner;
import com.example.demo.entity.Wallet;
import com.example.demo.entity.WalletTransaction;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PartnerRepository;
import com.example.demo.repository.WalletRepository;
import com.example.demo.repository.WalletTransactionRepository;
import com.example.demo.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final PartnerRepository partnerRepository;

    // Giả định Admin Wallet có ownerId = 1L
    private static final Long ADMIN_WALLET_OWNER_ID = 1L; 
    private static final String ADMIN_OWNER_TYPE = "ADMIN";
    private static final String PARTNER_OWNER_TYPE = "PARTNER";


    public WalletServiceImpl(WalletRepository walletRepository,
                             WalletTransactionRepository transactionRepository,
                             PartnerRepository partnerRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.partnerRepository = partnerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletByOwner(String ownerType, Long ownerId) {
        Wallet wallet = walletRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for ownerType: " + ownerType + " and ownerId: " + ownerId));
        return convertToWalletDTO(wallet);
    }

    @Override
    @Transactional
    public WalletTransactionResponseDTO adminTopupForPartner(WalletTopupRequestDTO topupRequest) {
        BigDecimal amount = topupRequest.getAmount();

        // 1. Tìm ví của Partner
        Partner partner = partnerRepository.findById(topupRequest.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + topupRequest.getPartnerId()));
        Wallet partnerWallet = partner.getWallet();
        if (partnerWallet == null) {
            throw new ResourceNotFoundException("Wallet not found for partner with id: " + topupRequest.getPartnerId());
        }

        // 2. Tìm ví của Admin (ví nguồn)
        Wallet adminWallet = walletRepository.findByOwnerTypeAndOwnerId(ADMIN_OWNER_TYPE, ADMIN_WALLET_OWNER_ID)
                .orElseThrow(() -> new IllegalStateException("Admin wallet is not configured."));

        // 3. Cập nhật số dư (Logic này có thể phức tạp hơn nếu cần kiểm tra số dư admin)
        // Lưu ý: Nhờ có @Version trên Wallet Entity, JPA sẽ tự động kiểm tra xung đột cập nhật
        partnerWallet.setBalance(partnerWallet.getBalance().add(amount));
        adminWallet.setBalance(adminWallet.getBalance().subtract(amount)); // Giả sử admin có đủ tiền
        
        walletRepository.save(partnerWallet);
        walletRepository.save(adminWallet);

        // 4. Tạo bản ghi giao dịch để lưu vết (từ góc nhìn của người nhận)
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(partnerWallet);
        transaction.setCounterparty(adminWallet);
        transaction.setTxnType("ADMIN_TOPUP");
        transaction.setAmount(amount);
        transaction.setReferenceType("ADMIN_ACTION"); // Tham chiếu đến hành động của admin

        WalletTransaction savedTransaction = transactionRepository.save(transaction);
        
        return convertToTransactionDTO(savedTransaction);
    }
    
    // Helper methods for DTO conversion
    private WalletResponseDTO convertToWalletDTO(Wallet wallet) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(wallet.getId());
        dto.setOwnerType(wallet.getOwnerType());
        dto.setOwnerId(wallet.getOwnerId());
        dto.setCurrency(wallet.getCurrency());
        dto.setBalance(wallet.getBalance());
        dto.setCreatedAt(wallet.getCreatedAt());
        return dto;
    }

    @Override
    @Transactional
    public void deductBalance(String ownerType, Long ownerId, BigDecimal amount, String referenceType, Long referenceId) {
        Wallet wallet = walletRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for ownerType: " + ownerType + " and ownerId: " + ownerId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setTxnType("DEBIT");
        transaction.setAmount(amount.negate()); // Negative for debit
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setIdempotencyKey(generateIdempotencyKey(referenceType, referenceId, "DEBIT"));

        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void refundBalance(String ownerType, Long ownerId, BigDecimal amount, String referenceType, Long referenceId) {
        Wallet wallet = walletRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for ownerType: " + ownerType + " and ownerId: " + ownerId));

        wallet.setBalance(wallet.getBalance().add(amount));

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setTxnType("CREDIT");
        transaction.setAmount(amount);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setIdempotencyKey(generateIdempotencyKey(referenceType, referenceId, "CREDIT"));

        transactionRepository.save(transaction);
    }

    private String generateIdempotencyKey(String referenceType, Long referenceId, String txnType) {
        return referenceType + "_" + referenceId + "_" + txnType + "_" + System.currentTimeMillis();
    }

    private WalletTransactionResponseDTO convertToTransactionDTO(WalletTransaction transaction) {
        WalletTransactionResponseDTO dto = new WalletTransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setTxnType(transaction.getTxnType());
        dto.setAmount(transaction.getAmount());
        dto.setReferenceType(transaction.getReferenceType());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setCreatedAt(transaction.getCreatedAt());
        if (transaction.getWallet() != null) {
            dto.setWalletId(transaction.getWallet().getId());
        }
        if (transaction.getCounterparty() != null) {
            dto.setCounterpartyId(transaction.getCounterparty().getId());
        }
        return dto;
    }
}