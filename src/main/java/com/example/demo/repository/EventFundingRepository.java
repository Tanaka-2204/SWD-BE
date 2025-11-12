package com.example.demo.repository;

import com.example.demo.entity.EventFunding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventFundingRepository extends JpaRepository<EventFunding, UUID> {
    List<EventFunding> findAllByEventId(UUID eventId);
}