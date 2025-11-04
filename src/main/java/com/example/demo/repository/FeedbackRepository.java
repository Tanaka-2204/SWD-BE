package com.example.demo.repository;

import com.example.demo.entity.Feedback;
import org.springframework.data.domain.Page;         // <<< THÊM
import org.springframework.data.domain.Pageable;      // <<< THÊM
import org.springframework.data.jpa.repository.EntityGraph; // <<< THÊM
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // (Hàm này bạn đã sửa ở bước trước, tải luôn Event/Student)
    @EntityGraph(attributePaths = {"student", "event"})
    Optional<Feedback> findByStudentIdAndEventId(Long studentId, Long eventId);

    /**
     * (API 1) Lấy feedback theo Event ID (đã phân trang)
     * Thêm @EntityGraph để fix lỗi LazyInit trong convertToDTO
     */
    @EntityGraph(attributePaths = {"student", "event"})
    Page<Feedback> findByEventId(Long eventId, Pageable pageable);

    /**
     * (API 2) Ghi đè hàm findAll để lấy tất cả (đã phân trang)
     * Thêm @EntityGraph để fix lỗi LazyInit trong convertToDTO
     */
    @Override
    @EntityGraph(attributePaths = {"student", "event"})
    Page<Feedback> findAll(Pageable pageable);
}