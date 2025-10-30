package com.example.demo.repository;

import com.example.demo.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findAllByEventIdAndVerifiedTrue(Long eventId);

    Optional<Checkin> findByEventIdAndStudentId(Long eventId, Long studentId);
}