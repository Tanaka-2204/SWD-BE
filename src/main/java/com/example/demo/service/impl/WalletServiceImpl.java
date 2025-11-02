package com.example.demo.service.impl;

import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import com.example.demo.service.WalletService;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private StudentRepository studentRepository;
    private final PartnerRepository partnerRepository;

    private static final Long ADMIN_WALLET_OWNER_ID = 1L; // Cấu hình Admin Wallet ID
    private static final String ADMIN_OWNER_TYPE = "ADMIN";

    public WalletServiceImpl(WalletRepository walletRepository, WalletTransactionRepository transactionRepository, StudentRepository studentRepository, PartnerRepository partnerRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
        this.partnerRepository = partnerRepository;
    }

    // --- READ OPERATIONS ---
    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletById(Long walletId) {
        Wallet wallet = findWalletByIdOrThrow(walletId);
        return convertToWalletDTO(wallet);
    }

    // =======================================================
    // == PHƯƠNG THỨC ĐƯỢC THÊM LẠI ĐỂ SỬA LỖI CONTROLLER ==
    // =======================================================
    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getWalletByOwner(String ownerType, Long ownerId) {
        Wallet wallet = walletRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for ownerType: " + ownerType + " and ownerId: " + ownerId));
        return convertToWalletDTO(wallet);
    }
    // =======================================================
    
    // (Hàm này được PartnerController gọi, giữ nguyên)
    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponseDTO> getTransactionHistory(Long ownerId, String ownerType, Pageable pageable) {
        
        // SỬA LỖI TẠI ĐÂY:
        // Lỗi của bạn là: walletRepository.findByOwnerIdAndOwnerType(ownerId, ownerType, pageable)
        // Sửa thành:
        Wallet wallet = walletRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId) 
                .orElseThrow(() -> new ResourceNotFoundException(ownerType + " wallet not found for ID: " + ownerId));
        
        // SỬA LỖI TẠI ĐÂY: (Gọi hàm 'findByWalletId' đã sửa ở Bước 2)
        return transactionRepository.findByWalletId(wallet.getId(), pageable)
                                          .map(this::convertToTransactionDTO); // (Dùng hàm helper của bạn)
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponseDTO> getTransactionHistoryForUser(AuthPrincipal principal, Pageable pageable) {
        
        Long walletId = null;

        // 1. Tìm Wallet ID dựa trên vai trò
        if (principal.isPartner()) {
            Partner partner = partnerRepository.findById(principal.getPartnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found for ID: " + principal.getPartnerId()));
            if(partner.getWallet() == null) throw new ResourceNotFoundException("Partner wallet not found.");
            walletId = partner.getWallet().getId();
            
        } else if (principal.isStudent()) {
            Student student = studentRepository.findById(principal.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for ID: " + principal.getStudentId()));
            if(student.getWallet() == null) throw new ResourceNotFoundException("Student wallet not found.");
            walletId = student.getWallet().getId();
        } else if (principal.isAdmin()) {
             throw new ForbiddenException("Admin does not have a personal wallet.");
        }

        if (walletId == null) {
            throw new ResourceNotFoundException("Wallet not found for the current user.");
        }

        // 2. Gọi Repo (SỬA LỖI: Gọi hàm 'findByWalletId' đã sửa ở Bước 2)
        return transactionRepository.findByWalletId(walletId, pageable)
                                          .map(this::convertToTransactionDTO); // (Dùng hàm helper của bạn)
    }
    
    // --- WRITE OPERATIONS ---
    @Override
    @Transactional
    public WalletTransactionResponseDTO adminTopupForPartner(WalletTopupRequestDTO topupRequest) {
         BigDecimal amount = topupRequest.getAmount();
         Partner partner = partnerRepository.findById(topupRequest.getPartnerId())
                 .orElseThrow(() -> new ResourceNotFoundException("Partner not found with id: " + topupRequest.getPartnerId()));
         // Sửa lại: Lấy ví từ owner thay vì ID trực tiếp, an toàn hơn
         Wallet partnerWallet = walletRepository.findByOwnerTypeAndOwnerId("PARTNER", partner.getId())
                 .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for partner: " + partner.getId()));
         Wallet adminWallet = walletRepository.findByOwnerTypeAndOwnerId(ADMIN_OWNER_TYPE, ADMIN_WALLET_OWNER_ID)
                 .orElseThrow(() -> new IllegalStateException("Admin wallet is not configured."));

         partnerWallet.setBalance(partnerWallet.getBalance().add(amount));
         adminWallet.setBalance(adminWallet.getBalance().subtract(amount));

         walletRepository.save(partnerWallet);
         walletRepository.save(adminWallet);

         WalletTransaction transaction = new WalletTransaction();
         transaction.setWallet(partnerWallet);
         transaction.setCounterparty(adminWallet);
         transaction.setTxnType("ADMIN_TOPUP");
         transaction.setAmount(amount);
         transaction.setReferenceType("ADMIN_ACTION");
         // Nên thêm Idempotency Key cho cả Topup
         // transaction.setIdempotencyKey(generateIdempotencyKey());

         WalletTransaction savedTransaction = transactionRepository.save(transaction);
         return convertToTransactionDTO(savedTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponseDTO transferCoins(WalletTransferRequestDTO request) {
        // Idempotency Check
        transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).ifPresent(tx -> {
             logger.warn("Idempotency key {} already processed. Returning existing transaction.", request.getIdempotencyKey());
             throw new DataIntegrityViolationException("Duplicate transaction: Idempotency key already used."); // Nên throw lỗi rõ ràng
        });


        BigDecimal amount = request.getAmount();
        Wallet fromWallet = findWalletByIdOrThrow(request.getFromWalletId());
        Wallet toWallet = findWalletByIdOrThrow(request.getToWalletId());

        if (fromWallet.getId().equals(toWallet.getId())) {
             throw new DataIntegrityViolationException("Cannot transfer to the same wallet.");
        }

        // Check balance
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new DataIntegrityViolationException("Insufficient funds in wallet " + fromWallet.getId());
        }

        // Perform transfer
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Record transaction (from perspective of the sender)
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(fromWallet);
        transaction.setCounterparty(toWallet);
        transaction.setTxnType("TRANSFER");
        transaction.setAmount(amount.negate()); // Ghi âm
        transaction.setIdempotencyKey(request.getIdempotencyKey());

        WalletTransaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transfer successful: {} coins from wallet {} to {}", amount, fromWallet.getId(), toWallet.getId());
        return convertToTransactionDTO(savedTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponseDTO redeemCoins(WalletRedeemRequestDTO request) {
        // Idempotency Check
        transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).ifPresent(tx -> {
             logger.warn("Idempotency key {} already processed for redeem. Returning existing transaction.", request.getIdempotencyKey());
             throw new DataIntegrityViolationException("Duplicate transaction: Idempotency key already used.");
        });

        BigDecimal amount = request.getAmount();
        Wallet studentWallet = findWalletByIdOrThrow(request.getStudentWalletId());

        // Check balance
        if (studentWallet.getBalance().compareTo(amount) < 0) {
            throw new DataIntegrityViolationException("Insufficient funds in student wallet " + studentWallet.getId());
        }

        // Deduct balance
        studentWallet.setBalance(studentWallet.getBalance().subtract(amount));
        walletRepository.save(studentWallet);

        // Record transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(studentWallet);
        transaction.setTxnType("REDEEM_PRODUCT");
        transaction.setAmount(amount.negate()); // Ghi âm
        transaction.setReferenceType("PRODUCT_INVOICE");
        transaction.setReferenceId(request.getReferenceId());
        transaction.setIdempotencyKey(request.getIdempotencyKey());

        WalletTransaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Redemption successful: {} coins from wallet {} for invoice {}", amount, studentWallet.getId(), request.getReferenceId());
        return convertToTransactionDTO(savedTransaction);
    }

    @Override
    @Transactional
    public WalletTransactionResponseDTO rollbackTransaction(WalletRollbackRequestDTO request) {
         // Idempotency Check for rollback itself
        transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).ifPresent(tx -> {
             logger.warn("Idempotency key {} already processed for rollback. Returning existing transaction.", request.getIdempotencyKey());
             throw new DataIntegrityViolationException("Duplicate transaction: Idempotency key already used.");
        });

        // 1. Find the original transaction
        WalletTransaction originalTx = transactionRepository.findById(request.getOriginalTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Original transaction not found: " + request.getOriginalTransactionId()));

        // TODO: Add more checks (e.g., if already rolled back, type allowed?)

        // 2. Determine wallets and amount to reverse
        Wallet walletToCredit = originalTx.getWallet();
        Wallet walletToDebit = originalTx.getCounterparty();
        BigDecimal amountToReverse = originalTx.getAmount().abs();

        // 3. Reverse balances
        walletToCredit.setBalance(walletToCredit.getBalance().add(amountToReverse));
        walletRepository.save(walletToCredit);

        if (walletToDebit != null) {
            // Kiểm tra ví debit còn đủ tiền để trừ lại không
            if (walletToDebit.getBalance().compareTo(amountToReverse) < 0) {
                logger.error("Cannot rollback transaction {}: Debit wallet {} has insufficient funds.", originalTx.getId(), walletToDebit.getId());
                throw new DataIntegrityViolationException("Insufficient funds in counterparty wallet for rollback.");
            }
            walletToDebit.setBalance(walletToDebit.getBalance().subtract(amountToReverse));
            walletRepository.save(walletToDebit);
        }

        // 4. Record the rollback transaction
        WalletTransaction rollbackTx = new WalletTransaction();
        rollbackTx.setWallet(walletToCredit);
        rollbackTx.setCounterparty(walletToDebit);
        rollbackTx.setTxnType("ROLLBACK"); // Hoặc REFUND
        rollbackTx.setAmount(amountToReverse); // Cộng lại tiền
        rollbackTx.setReferenceType("WALLET_TRANSACTION");
        rollbackTx.setReferenceId(originalTx.getId());
        rollbackTx.setIdempotencyKey(request.getIdempotencyKey());

        WalletTransaction savedRollbackTx = transactionRepository.save(rollbackTx);
        logger.info("Rollback successful for original transaction {}: {} coins credited to wallet {}", originalTx.getId(), amountToReverse, walletToCredit.getId());
        return convertToTransactionDTO(savedRollbackTx);
    }

    // --- HELPER METHODS ---
    private Wallet findWalletByIdOrThrow(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionResponseDTO> getAllTransactions(Pageable pageable) {
        Page<WalletTransaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return transactions.map(this::convertToTransactionDTO);
    }
}