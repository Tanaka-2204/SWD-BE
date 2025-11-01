package com.example.demo.repository;

import com.example.demo.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletId(Long walletId, Pageable pageable);

    /**
     * Finds a transaction by its idempotency key to prevent duplicate operations.
     */
    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Finds the original transaction that needs to be rolled back/refunded.
     * Optionally check if it has already been refunded.
     */
    // Optional<WalletTransaction> findByIdAndRefundTxnIdIsNull(Long id); // Ví dụ kiểm tra đã refund chưa
}