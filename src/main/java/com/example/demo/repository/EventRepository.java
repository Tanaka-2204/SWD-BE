package com.example.demo.repository;

import com.example.demo.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    Page<Event> findAllByPartnerId(UUID partnerId, Pageable pageable);

    List<Event> findAllByCategoryId(UUID categoryId);

    Page<Event> findAllByStartTimeAfter(OffsetDateTime currentTime, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startTime <= :currentTime AND e.endTime >= :currentTime")
    List<Event> findOngoingEvents(OffsetDateTime currentTime);

    Page<Event> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT e.partner.name, COUNT(e.id) FROM Event e GROUP BY e.partner.name ORDER BY COUNT(e.id) DESC")
    java.util.List<Object[]> findPartnerActivity();
}