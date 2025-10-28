package com.example.demo.repository;

import com.example.demo.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * Finds a specific registration by student and event.
     * Used to check if a student is already registered for an event.
     */
    Optional<Registration> findByStudentIdAndEventId(Long studentId, Long eventId);

    /**
     * Finds all registrations for a specific event.
     * Used to get the list of all attendees for a broadcast.
     */
    List<Registration> findAllByEventId(Long eventId);

    /**
     * Finds a paginated list of registrations for a specific event.
     * Used to get the list of attendees with pagination.
     */
    Page<Registration> findAllByEventId(Long eventId, Pageable pageable);
}