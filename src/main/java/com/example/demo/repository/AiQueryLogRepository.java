package com.example.demo.repository;

import com.example.demo.entity.AiQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiQueryLogRepository extends JpaRepository<AiQueryLog, Long> {
}