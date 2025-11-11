package com.example.demo.repository;

import com.example.demo.entity.Checkin;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, UUID> {

    List<Checkin> findAllByEventIdAndVerifiedTrue(UUID eventId);
    boolean existsByEventIdAndStudentId(UUID eventId, UUID studentId);
    Optional<Checkin> findByEventIdAndStudentId(UUID eventId, UUID studentId);
    List<Checkin> findAllByEventId(UUID eventId);
    Page<Checkin> findAllByEventId(UUID eventId, Pageable pageable);
    @EntityGraph(attributePaths = {
        "event",            // Tải Event
        "event.partner",      // Tải Partner (Fix lỗi lúc 23:55)
        "event.category" // Tải Category (Fix lỗi lúc 00:02)
    })
    Page<Checkin> findByStudentId(UUID studentId, Pageable pageable);
    
    Integer countByEventId(UUID eventId);
}