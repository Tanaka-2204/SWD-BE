package com.example.demo.repository;

import com.example.demo.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    /**
     * Tìm một bản ghi check-in cụ thể bằng Event ID và Student ID.
     * Dùng để kiểm tra xem sinh viên đã check-in sự kiện này trước đó hay chưa.
     *
     * @param eventId ID của sự kiện
     * @param studentId ID của sinh viên
     * @return Optional<Checkin>
     */
    Optional<Checkin> findByEventIdAndStudentId(Long eventId, Long studentId);
}