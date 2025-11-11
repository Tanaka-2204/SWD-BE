package com.example.demo.repository;

import com.example.demo.entity.BroadcastDelivery;
import com.example.demo.entity.enums.BroadcastDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BroadcastDeliveryRepository extends JpaRepository<BroadcastDelivery, UUID> {

    @EntityGraph(attributePaths = {"broadcast", "broadcast.event"})
    Page<BroadcastDelivery> findByStudentId(UUID studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"broadcast", "broadcast.event"})
    Page<BroadcastDelivery> findByStudentIdAndStatus(UUID studentId, BroadcastDeliveryStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"broadcast", "broadcast.event"})
    Optional<BroadcastDelivery> findByIdAndStudentId(UUID deliveryId, UUID studentId);

    long countByStudentIdAndStatus(UUID studentId, BroadcastDeliveryStatus status);
}