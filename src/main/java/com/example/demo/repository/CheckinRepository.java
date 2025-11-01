package com.example.demo.repository;

import com.example.demo.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findAllByEventIdAndVerifiedTrue(Long eventId);
    boolean existsByEventIdAndStudentId(Long eventId, Long studentId);
    Optional<Checkin> findByEventIdAndStudentId(Long eventId, Long studentId);
    List<Checkin> findAllByEventId(Long eventId);
    Page<Checkin> findAllByEventId(Long eventId, Pageable pageable);
    Page<Checkin> findByStudentId(Long studentId, Pageable pageable);
    Integer countByEventId(Long eventId);
}