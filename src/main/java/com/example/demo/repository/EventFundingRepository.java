package com.example.demo.repository;

import com.example.demo.entity.EventFunding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventFundingRepository extends JpaRepository<EventFunding, Long> {
}