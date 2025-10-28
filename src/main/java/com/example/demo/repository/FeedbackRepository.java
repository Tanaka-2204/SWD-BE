package com.example.demo.repository;

import com.example.demo.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Tìm một bản ghi feedback cụ thể dựa trên Student ID và Event ID.
     * Dùng để kiểm tra xem sinh viên đã gửi feedback cho sự kiện này hay chưa.
     *
     * @param studentId ID của sinh viên
     * @param eventId ID của sự kiện
     * @return Optional<Feedback>
     */
    Optional<Feedback> findByStudentIdAndEventId(Long studentId, Long eventId);
}