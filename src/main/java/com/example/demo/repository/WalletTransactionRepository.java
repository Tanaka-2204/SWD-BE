package com.example.demo.repository;

import com.example.demo.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    Page<WalletTransaction> findByWalletId(UUID walletId, Pageable pageable);

    /**
     * Finds a transaction by its idempotency key to prevent duplicate operations.
     */
    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Finds the original transaction that needs to be rolled back/refunded.
     * Optionally check if it has already been refunded.
     */
    // Optional<WalletTransaction> findByIdAndRefundTxnIdIsNull(UUID id); // Ví dụ kiểm tra đã refund chưa

    /**
     * Finds all transactions for admin monitoring, ordered by creation date descending.
     */
    Page<WalletTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<WalletTransaction> findByWalletIdOrCounterpartyId(UUID walletId, UUID counterpartyId);
}